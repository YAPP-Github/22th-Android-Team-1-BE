package com.yapp.cvs.api.like.dto

import com.yapp.cvs.domain.enums.ProductLikeType
import com.yapp.cvs.domain.like.entity.ProductLikeHistory
import com.yapp.cvs.domain.like.vo.ProductLikeSummaryVO

data class ProductLikeSummaryDTO(
        val productId: Long,
        val likeCount: Long,
        val dislikeCount: Long,
        val totalCount: Long
) {
    companion object {
        fun from(productLikeSummaryVO: ProductLikeSummaryVO): ProductLikeSummaryDTO {
            return ProductLikeSummaryDTO(
                    productId = productLikeSummaryVO.productId,
                    likeCount = productLikeSummaryVO.likeCount,
                    dislikeCount = productLikeSummaryVO.dislikeCount,
                    totalCount = productLikeSummaryVO.totalCount
            )
        }
    }
}