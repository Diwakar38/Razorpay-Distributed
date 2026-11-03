package com.projects.razorpay.merchant_service.mapper;

import com.projects.razorpay.merchant_service.dto.response.ApiKeyCreateResponse;
import com.projects.razorpay.merchant_service.dto.response.ApiKeyRespose;
import com.projects.razorpay.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    ApiKeyCreateResponse toCreateResponse(ApiKey apiKey);

    List<ApiKeyRespose> toApiKeyResponseList(List<ApiKey> apiKeyList);

}
