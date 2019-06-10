package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (product.getSubImages() != null) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }

            if (product.getId() != null) {
                int rowCount = productMapper.updateByPrimaryKeySelective(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");
            } else {
                int rowCount = productMapper.insertSelective(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("新增产品成功");
                }
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("参数传递错误");
    }

    public ServerResponse setSaleStatus(Integer productId, Integer productStatus) {
        System.out.println(productId+productStatus);
        if (productId == null || productStatus == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(productStatus);
        int nowCount = productMapper.updateByPrimaryKeySelective(product);
        if (nowCount > 0) {
            return ServerResponse.createBySuccessMessage("更新产品状态成功");
        }
        return ServerResponse.createByErrorMessage("更新产品状态失败");
    }

    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }


    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setParentCategoryId(product.getCategoryId());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dataToStr(product.getCreateTime()));
        productDetailVo.setCreateTime(DateTimeUtil.dataToStr(product.getCreateTime()));
        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVos = new ArrayList<ProductListVo>();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVos);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));

        return productListVo;
    }


    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByProductNameAndProductId(productName, productId);
        List<ProductListVo> productListVos = new ArrayList<ProductListVo>();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVos);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<ProductDetailVo> productDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVos = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVos);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getParentId()).getData();
        }

        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }

        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword, categoryIdList.size() == 0 ? null : categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
