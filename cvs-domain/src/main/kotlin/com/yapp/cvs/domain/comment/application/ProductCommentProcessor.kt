package com.yapp.cvs.domain.comment.application

import com.yapp.cvs.domain.base.vo.OffsetPageVO
import com.yapp.cvs.domain.comment.vo.ProductCommentDetailVO
import com.yapp.cvs.domain.comment.vo.ProductCommentRequestVO
import com.yapp.cvs.domain.comment.vo.ProductCommentSearchVO
import com.yapp.cvs.domain.comment.vo.ProductCommentVO
import com.yapp.cvs.domain.enums.DistributedLockType
import com.yapp.cvs.infrastructure.redis.lock.DistributedLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductCommentProcessor(
        private val productCommentService: ProductCommentService,
) {
    fun getComment(commentId: Long): ProductCommentVO {
        return ProductCommentVO.from(productCommentService.findProductComment(commentId))
    }

    fun getCommentDetails(productCommentSearchVO: ProductCommentSearchVO): OffsetPageVO<ProductCommentDetailVO> {
        val result = productCommentService.getProductCommentsPage(productCommentSearchVO)
        return OffsetPageVO(result.lastOrNull()?.productCommentId, result)
    }

    @DistributedLock(DistributedLockType.MEMBER_PRODUCT, ["productCommentRequestVO"])
    fun createComment(productCommentRequestVO: ProductCommentRequestVO, content: String): ProductCommentVO {
        val commentHistory = productCommentService.write(productCommentRequestVO.memberProductMappingKey, content)
        return ProductCommentVO.from(commentHistory)
    }

    @DistributedLock(DistributedLockType.MEMBER_PRODUCT, ["productCommentRequestVO"])
    fun updateComment(productCommentRequestVO: ProductCommentRequestVO, content: String): ProductCommentVO {
        val commentHistory = productCommentService.update(productCommentRequestVO.memberProductMappingKey, content)
        return ProductCommentVO.from(commentHistory)
    }
}
