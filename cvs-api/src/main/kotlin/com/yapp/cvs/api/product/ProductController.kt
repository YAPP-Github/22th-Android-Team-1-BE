package com.yapp.cvs.api.product

import com.yapp.cvs.api.common.dto.OffsetPageDTO
import com.yapp.cvs.api.product.dto.ProductDTO
import com.yapp.cvs.api.product.dto.ProductDetailDTO
import com.yapp.cvs.api.product.dto.ProductSearchDTO
import com.yapp.cvs.domain.product.application.ProductProcessor
import org.springdoc.api.annotations.ParameterObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/product")
class ProductController(
    private val productProcessor: ProductProcessor
) {
    @GetMapping("/{productId}/detail")
    fun getProductDetail(@PathVariable productId: Long): ProductDetailDTO {
        return ProductDetailDTO.from(productProcessor.getProductDetail(productId, 1L))
    }

    @GetMapping("/search")
    fun searchProductList(@ParameterObject productSearchDTO: ProductSearchDTO): OffsetPageDTO<ProductDTO> {
        val result = productProcessor.searchProductPageList(productSearchDTO.toOffsetVO(), productSearchDTO.toVO())
        return OffsetPageDTO(result.lastId, result.content.map { ProductDTO.from(it) })
    }
}
