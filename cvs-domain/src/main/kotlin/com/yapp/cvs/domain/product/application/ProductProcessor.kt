package com.yapp.cvs.domain.product.application

import com.yapp.cvs.domain.base.vo.OffsetPageVO
import com.yapp.cvs.domain.base.vo.OffsetSearchVO
import com.yapp.cvs.domain.base.vo.PageSearchVO
import com.yapp.cvs.domain.base.vo.PageVO
import com.yapp.cvs.domain.like.application.MemberProductLikeMappingService
import com.yapp.cvs.domain.like.entity.MemberProductLikeMapping
import com.yapp.cvs.domain.like.entity.MemberProductMappingKey
import com.yapp.cvs.domain.product.vo.ProductDetailVO
import com.yapp.cvs.domain.product.vo.ProductSearchVO
import com.yapp.cvs.domain.product.vo.ProductUpdateVO
import com.yapp.cvs.domain.product.vo.ProductVO
import org.springframework.stereotype.Service
import java.security.PrivateKey
import javax.transaction.Transactional

@Service
@Transactional
class ProductProcessor(
    private val productService: ProductService,
    private val memberProductLikeMappingService: MemberProductLikeMappingService
) {
    fun getProductDetail(productId: Long, memberId: Long): ProductDetailVO {
        val product = productService.findProduct(productId)

        val memberProductLikeMapping = memberProductLikeMappingService.findByMemberProductLike(
            MemberProductMappingKey(productId, memberId)
        )

        productService.increaseProductViewCount(productId)

        return ProductDetailVO.from(product, memberProductLikeMapping)
    }

    fun searchProductPageList(offsetSearchVO: OffsetSearchVO, productSearchVO: ProductSearchVO): OffsetPageVO<ProductVO> {
        val productList = productService.searchProductList(offsetSearchVO, productSearchVO)

        return OffsetPageVO(
            productList.lastOrNull()?.productId,
            productList.map { ProductVO.from(it) }
        )
    }

    fun searchProductPage(pageSearchVO: PageSearchVO, productSearchVO: ProductSearchVO): PageVO<ProductVO> {
        val result = productService.searchProductPage(pageSearchVO, productSearchVO)

        return PageVO(result.pageable.pageNumber, result.pageable.pageSize, result.totalPages,
            result.content.map { ProductVO.from(it) }
        )
    }

    fun updateProduct(productId: Long, productUpdateVO: ProductUpdateVO) {
        val product = productService.findProductById(productId)
            ?: throw RuntimeException("not found product")

        product.update(productUpdateVO)
        productService.saveProduct(product)
    }
}