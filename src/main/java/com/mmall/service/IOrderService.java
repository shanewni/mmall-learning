package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Long orderNo, Integer UserId, String path);

    ServerResponse aliCallBack(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
}
