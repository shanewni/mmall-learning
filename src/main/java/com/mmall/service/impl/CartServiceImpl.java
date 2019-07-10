package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> add(Integer count, Integer userId, Integer productId) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setQuantity(count);
            cartMapper.insert(cartItem);
        } else {
           count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    public ServerResponse<CartVo> update(Integer count, Integer userId, Integer productId) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);
    }

    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        List<String> productArray = Splitter.on(".").splitToList(productIds);
        if (CollectionUtils.isEmpty(productArray)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId, productArray);
        return this.list(userId);
    }

    public ServerResponse<CartVo> selectAll(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.getCartProductCount(userId));
    }

    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        for (Cart cartItem : cartList) {
            CartProductVo cartProductVoItem = new CartProductVo();
            cartProductVoItem.setId(cartItem.getId());
            cartProductVoItem.setUserId(cartItem.getUserId());
            cartProductVoItem.setProductId(cartItem.getProductId());
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (product != null) {
                cartProductVoItem.setProductMainImage(product.getMainImage());
                cartProductVoItem.setProductName(product.getName());
                cartProductVoItem.setProductSubtitle(product.getSubtitle());
                cartProductVoItem.setProductStatus(product.getStatus());
                cartProductVoItem.setProductPrice(product.getPrice());
                cartProductVoItem.setProductStock(product.getStock());

                int buyLimitCount = 0;
                if (product.getStock() >= cartItem.getQuantity()) {
                    buyLimitCount = cartItem.getQuantity();
                    cartProductVoItem.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                } else {
                    buyLimitCount = product.getStock();
                    cartProductVoItem.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    Cart cartForQuantity = new Cart();
                    cartForQuantity.setId(cartItem.getId());
                    cartForQuantity.setQuantity(buyLimitCount);
                    cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                }
                cartProductVoItem.setQuantity(buyLimitCount);
                cartProductVoItem.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVoItem.getQuantity()));
                cartProductVoItem.setProductChecked(cartItem.getChecked());
            }
            if (cartItem.getChecked() == Const.Cart.CHECKED) {
                //如果已经勾选,增加到整个的购物车总价中
                cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVoItem.getProductTotalPrice().doubleValue());
            }
            cartProductVoList.add(cartProductVoItem);
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }

}
