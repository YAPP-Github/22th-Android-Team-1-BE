package com.yapp.cvs.job.crawl.cu

import com.yapp.cvs.domains.product.ProductService
import com.yapp.cvs.job.config.BatchConfig
import com.yapp.cvs.job.crawl.ProductCollectorService
import com.yapp.cvs.job.crawl.instruction.CUWebdriverInstruction
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConditionalOnProperty(
    value = [BatchConfig.SPRING_BATCH_JOB_NAMES],
    havingValue = CUCollectorConfig.JOB_NAME,
)
@Configuration
class CUCollectorConfig(
    private val jobBuilderFactory: JobBuilderFactory,
    private val jobRepository: JobRepository,
    private val stepBuilderFactory: StepBuilderFactory,
    private val productService: ProductService,
    private val webdriverInstruction: CUWebdriverInstruction,
) {
    companion object {
        const val JOB_NAME = "cu-collect-job"
        const val STEP_NAME = "cu-collect-job-step"
    }

    @Bean
    fun cuCollectorJob(): Job {
        return jobBuilderFactory[JOB_NAME]
            .repository(jobRepository)
            .start(cuCollectorStep())
            .incrementer(RunIdIncrementer())
            .build()
    }

    @Bean
    @JobScope
    fun cuCollectorStep(): Step {
        return stepBuilderFactory[STEP_NAME]
            .tasklet(cuCollectorTasklet())
            .transactionManager(ResourcelessTransactionManager())
            .build()
    }

    @Bean
    @StepScope
    fun cuCollectorTasklet(): Tasklet = CUCollectorTasklet(
        cuCollectorService = cuCollectorService(),
    )

    @Bean
    fun cuCollectorService(): ProductCollectorService = CUCollectorService(
        productService = productService,
        webdriverInstruction = webdriverInstruction,
    )
}
