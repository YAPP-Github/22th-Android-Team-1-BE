package com.yapp.cvs.domain.collect.application

import com.yapp.cvs.domain.collect.ProductRawDataVO
import com.yapp.cvs.domain.extension.endOfMonth
import com.yapp.cvs.domain.like.entity.ProductLikeSummary
import com.yapp.cvs.domain.like.repository.ProductLikeSummaryRepository
import com.yapp.cvs.domain.product.entity.PbProductMapping
import com.yapp.cvs.domain.product.entity.ProductPromotion
import com.yapp.cvs.domain.product.entity.ProductPromotionType
import com.yapp.cvs.domain.product.entity.ProductRetailerMapping
import com.yapp.cvs.domain.product.repository.PbProductMappingRepository
import com.yapp.cvs.domain.product.repository.ProductPromotionRepository
import com.yapp.cvs.domain.product.repository.ProductRepository
import com.yapp.cvs.domain.product.repository.ProductRetailerMappingRepository
import com.yapp.cvs.infrastructure.s3.service.S3Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class ProductDataProcessor(
    private val productRepository: ProductRepository,
    private val productRetailerMappingRepository: ProductRetailerMappingRepository,
    private val pbProductMappingRepository: PbProductMappingRepository,
    private val productPromotionRepository: ProductPromotionRepository,
    private val productLikeSummaryRepository: ProductLikeSummaryRepository,
    private val s3Service: S3Service
) {
    @Transactional
    fun saveProduct(productRawDataVO: ProductRawDataVO) {
        if (productRepository.findByBarcode(productRawDataVO.barcode) == null) {
            val imageUrl = s3Service.copyAndUploadIfExist(productRawDataVO.imageUrl, productRawDataVO.barcode)
            val productEntity = productRepository.save(productRawDataVO.to(imageUrl))
            productRetailerMappingRepository.save(
                ProductRetailerMapping.of(
                    productEntity,
                    productRawDataVO.retailerType
                )
            )

            if (productRawDataVO.isPbProduct) {
                pbProductMappingRepository.save(PbProductMapping.of(productEntity))
            }

            productLikeSummaryRepository.save(ProductLikeSummary.empty(productEntity.productId!!))
        }
    }

    @Transactional
    fun savePromotion(productRawDataVO: ProductRawDataVO, productPromotionType: ProductPromotionType) {
        val product = productRepository.findByBarcode(productRawDataVO.barcode)
        if (product == null) {
            log.info("해당 상품에 대한 정보가 없습니다. barcode: ${productRawDataVO.barcode} name: ${productRawDataVO.name}")
        } else {
            productPromotionRepository.save(
                ProductPromotion(
                    productId = product.productId!!,
                    promotionType = productPromotionType,
                    retailerType = productRawDataVO.retailerType,
                    validAt = LocalDateTime.now().endOfMonth()
                )
            )
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
