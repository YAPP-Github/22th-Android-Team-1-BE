package com.yapp.cvs.api.comment

import com.yapp.cvs.api.comment.dto.ProductCommentContentDTO
import com.yapp.cvs.api.comment.dto.ProductCommentDTO
import com.yapp.cvs.api.comment.dto.ProductCommentDetailDTO
import com.yapp.cvs.api.comment.dto.ProductCommentSearchDTO
import com.yapp.cvs.api.common.dto.OffsetPageDTO
import com.yapp.cvs.domain.comment.application.ProductCommentProcessor
import com.yapp.cvs.domain.comment.vo.ProductCommentRequestVO
import com.yapp.cvs.domain.like.application.ProductLikeProcessor
import com.yapp.cvs.domain.like.vo.ProductLikeRequestVO
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.api.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/product")
class ProductCommentController(
        private val productCommentProcessor: ProductCommentProcessor,
        private val productLikeProcessor: ProductLikeProcessor
) {
    private val memberId = 1L //TODO : security context 에서 memberId 가져오기

    @GetMapping("/comments")
    @Operation(summary = "해당 상품에 작성된 평가 코멘트를 최대 10개 가져옵니다.")
    fun getRecentProductComments(@ParameterObject productCommentSearchDTO: ProductCommentSearchDTO): OffsetPageDTO<ProductCommentDetailDTO> {
        val requestVO = productCommentSearchDTO.toVO()
        val result = productCommentProcessor.getCommentDetails(memberId, requestVO)
        return OffsetPageDTO(result.lastId, result.content.map { ProductCommentDetailDTO.from(it) })
    }

    @GetMapping("/{productId}/comments")
    @Operation(summary = "해당 상품에 작성된 평가 코멘트를 조건만큼 가져옵니다.")
    fun getProductComments(@PathVariable productId: Long,
                           @ParameterObject productCommentSearchDTO: ProductCommentSearchDTO): OffsetPageDTO<ProductCommentDetailDTO> {
        val requestVO = productCommentSearchDTO.toVO(productId)
        val result = productCommentProcessor.getCommentDetails(memberId, requestVO)
        return OffsetPageDTO(result.lastId, result.content.map { ProductCommentDetailDTO.from(it) })
    }

    @PostMapping("/{productId}/comment/write")
    @Operation(summary = "상품에 대한 평가 코멘트를 작성합니다.")
    fun writeComment(@PathVariable productId: Long,
                     @RequestBody productCommentContentDTO: ProductCommentContentDTO): ProductCommentDTO {
        val requestVO = ProductCommentRequestVO(productId, memberId)
        val comment = productCommentProcessor.createComment(requestVO, productCommentContentDTO.content)
        return ProductCommentDTO.from(comment)
    }

    @PostMapping("/{productId}/comment/edit")
    @Operation(summary = "상품에 대한 평가 코멘트를 수정합니다.")
    fun updateComment(@PathVariable productId: Long,
                      @RequestBody productCommentContentDTO: ProductCommentContentDTO): ProductCommentDTO {
        val requestVO = ProductCommentRequestVO(productId, memberId)
        val comment = productCommentProcessor.updateComment(requestVO, productCommentContentDTO.content)
        return ProductCommentDTO.from(comment)
    }

    @PostMapping("/{productId}/comment/delete")
    @Operation(summary = "상품에 대한 평가 코멘트를 삭제합니다.")
    fun deleteComment(@PathVariable productId: Long) {
        productLikeProcessor.cancelEvaluation(ProductLikeRequestVO(productId = productId, memberId = memberId))
    }
}