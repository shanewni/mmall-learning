package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    public CategoryMapper categoryMapper;

    public Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || parentId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("添加分类成功");
        }
        return ServerResponse.createByErrorMessage("添加分类失败");
    }


    public ServerResponse updateCategory(String categoryName, Integer categoryId) {
        if (StringUtils.isBlank(categoryName) || categoryId == null) {
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(categoryId);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
           return ServerResponse.createBySuccessMessage("更新品类成功");
        }
        return ServerResponse.createByErrorMessage("更新品类失败");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(list)) {
            logger.error("未查询到分类");
        }
        return ServerResponse.createBySuccess(list);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorieSet = Sets.newHashSet();
        findChildCategory(categorieSet, categoryId);

        List<Integer> list = Lists.newArrayList();
        if (categoryId != null) {
            for (Category categoryItem : categorieSet) {
                list.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(list);
    }

    public Set<Category> findChildCategory(Set<Category> categories, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categories.add(category);
        }
        List<Category> list = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : list) {
            findChildCategory(categories,categoryItem.getId());
        }
        return categories;
    }
}
