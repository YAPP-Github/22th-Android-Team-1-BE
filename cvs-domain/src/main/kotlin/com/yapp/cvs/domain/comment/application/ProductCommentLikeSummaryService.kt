package com.yapp.cvs.domain.comment.application

import com.yapp.cvs.domain.comment.entity.ProductCommentLikeSummary
import com.yapp.cvs.domain.comment.repository.ProductCommentLikeSummaryRepository
import com.yapp.cvs.domain.comment.vo.ProductCommentLikeSummaryVO
import com.yapp.cvs.domain.enums.DistributedLockType
import com.yapp.cvs.domain.like.entity.MemberProductMappingKey
import com.yapp.cvs.infrastructure.redis.lock.DistributedLock
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ProductCommentLikeSummaryService(
        val productCommentLikeSummaryRepository: ProductCommentLikeSummaryRepository
) {
    fun getCommentLikeSummary(memberProductMappingKey: MemberProductMappingKey): ProductCommentLikeSummaryVO {
        return ProductCommentLikeSummaryVO.from(findOrEmpty(memberProductMappingKey))
    }

    @Async(value = "productCommentLikeSummaryTaskExecutor")
    @DistributedLock(type = DistributedLockType.COMMENT_LIKE, keys = ["memberProductMappingKey"])
    fun increaseLikeCount(memberProductMappingKey: MemberProductMappingKey) {
        val summary = findOrEmpty(memberProductMappingKey)
        summary.like()
        productCommentLikeSummaryRepository.save(summary)
    }

    @Async(value = "productCommentLikeSummaryTaskExecutor")
    @DistributedLock(type = DistributedLockType.COMMENT_LIKE, keys = ["memberProductMappingKey"])
    fun cancelLikeCount(memberProductMappingKey: MemberProductMappingKey) {
        val summary = findOrEmpty(memberProductMappingKey)
        summary.cancelLike()
        productCommentLikeSummaryRepository.save(summary)
    }

    private fun findOrEmpty(memberProductMappingKey: MemberProductMappingKey): ProductCommentLikeSummary {
        return productCommentLikeSummaryRepository
                .findByProductIdAndMemberId(memberProductMappingKey.productId, memberProductMappingKey.productId)
                ?: ProductCommentLikeSummary.empty(memberProductMappingKey)
    }
}
