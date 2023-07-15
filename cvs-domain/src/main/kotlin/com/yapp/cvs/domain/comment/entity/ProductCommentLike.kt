package com.yapp.cvs.domain.comment.entity

import com.yapp.cvs.domain.base.BaseEntity
import com.yapp.cvs.domain.like.entity.MemberProductMappingKey
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "product_comment_likes")
class ProductCommentLike(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val productCommentLikeId: Long? = null,

        val productId: Long,

        val memberId: Long,

        val likeMemberId: Long,

        var valid: Boolean
): BaseEntity() {
        companion object {
                fun like(memberProductMappingKey: MemberProductMappingKey, memberId: Long): ProductCommentLike {
                        return ProductCommentLike(
                                productId = memberProductMappingKey.productId,
                                memberId = memberProductMappingKey.memberId,
                                likeMemberId = memberId,
                                valid = true
                        )
                }
        }
}
