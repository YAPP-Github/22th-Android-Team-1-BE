package com.yapp.cvs.domain.like.application

import com.yapp.cvs.domain.enums.ProductLikeType
import com.yapp.cvs.domain.like.entity.MemberProductLikeMapping
import com.yapp.cvs.domain.like.entity.ProductLikeHistory
import com.yapp.cvs.domain.like.repository.MemberProductLikeMappingRepository
import com.yapp.cvs.domain.like.repository.ProductLikeHistoryRepository
import com.yapp.cvs.exception.BadRequestException
import org.springframework.stereotype.Service

@Service
class ProductLikeHistoryService(
        private val productLikeHistoryRepository: ProductLikeHistoryRepository
) {
    fun findLatest(productId: Long, memberId: Long): ProductLikeHistory? {
        return productLikeHistoryRepository.findLatestByProductIdAndMemberId(productId, memberId)
    }

    fun like(productId: Long, memberId: Long): ProductLikeHistory {
        val rating = ProductLikeHistory.like(productId, memberId)
        return productLikeHistoryRepository.save(rating)
    }

    fun dislike(productId: Long, memberId: Long): ProductLikeHistory {
        val rating = ProductLikeHistory.dislike(productId, memberId)
        return productLikeHistoryRepository.save(rating)
    }

    fun cancel(productId: Long, memberId: Long): ProductLikeHistory {
        val rating = ProductLikeHistory.none(productId, memberId)
        return productLikeHistoryRepository.save(rating)
    }
}
