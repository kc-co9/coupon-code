package com.co.kc.couponcode.provider.repository.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/alibaba/druid https://blog.csdn.net/u010223407/article/details/88427054
 * https://www.jianshu.com/p/c8a01ae9f779
 * @author kc
 */
@Configuration
public class DruidConfig {
    @Bean
    @Primary
    public DataSource dataSource(DruidProperties druidProperties, DatasourceProperties datasourceProperties) throws SQLException {
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(datasourceProperties.getUrl());
        datasource.setUsername(datasourceProperties.getUsername());
        datasource.setPassword(datasourceProperties.getPassword());
        datasource.setDriverClassName(datasourceProperties.getDriverClassName());

        datasource.setInitialSize(druidProperties.getInitialSize());
        datasource.setMinIdle(druidProperties.getMinIdle());
        datasource.setMaxActive(druidProperties.getMaxActive());
        datasource.setMaxWait(druidProperties.getMaxWait());
        datasource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        datasource.setValidationQuery(druidProperties.getValidationQuery());
        datasource.setTestWhileIdle(druidProperties.getTestWhileIdle());
        datasource.setTestOnBorrow(druidProperties.getTestOnBorrow());
        datasource.setTestOnReturn(druidProperties.getTestOnReturn());
        datasource.setPoolPreparedStatements(druidProperties.getPoolPreparedStatements());
        datasource.setMaxPoolPreparedStatementPerConnectionSize(
                druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        datasource.setFilters(druidProperties.getFilters());
        datasource.setConnectionProperties(druidProperties.getConnectionProperties());
        datasource.setUseGlobalDataSourceStat(druidProperties.getUseGlobalDataSourceStat());

        return datasource;
    }

    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean<StatViewServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setServlet(new StatViewServlet());
        servletRegistrationBean.addUrlMappings("/druid/*");

        Map<String, String> initParameters = new HashMap<>(10);
        // 设置控制台管理用户
        // 用户名, 密码
        initParameters.put("loginUsername", "admin");
        initParameters.put("loginPassword", "admin");
        // 禁用HTML页面上的“Reset All”功能
        // 是否可以重置数据
        initParameters.put("resetEnable", "false");
        // IP白名单 (没有配置或者为空，则允许所有访问)
        initParameters.put("allow", "");
        // IP黑名单 (存在共同时，deny优先于allow) 黑名单优先级高于白名单
        initParameters.put("deny", "192.168.20.38");
        servletRegistrationBean.setInitParameters(initParameters);
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        // 创建过滤器
        FilterRegistrationBean<WebStatFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new WebStatFilter());
        // 设置过滤器过滤路径
        filterRegistrationBean.addUrlPatterns("/*");
        // 忽略过滤的形式
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

    @Data
    @Configuration
    @ConfigurationProperties("spring.datasource")
    public static class DatasourceProperties {
        private String driverClassName;
        private String url;
        private String username;
        private String password;
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "druid")
    public static class DruidProperties {
        private Integer initialSize;
        private Integer minIdle;
        private Integer maxActive;
        private String filters;
        private Integer maxWait;
        private Integer timeBetweenEvictionRunsMillis;
        private Integer minEvictableIdleTimeMillis;
        private String validationQuery;
        private Boolean testWhileIdle;
        private Boolean testOnBorrow;
        private Boolean testOnReturn;
        private Boolean poolPreparedStatements;
        private Integer maxOpenPreparedStatments;
        private Integer maxPoolPreparedStatementPerConnectionSize;
        private String connectionProperties;
        private Boolean useGlobalDataSourceStat;
    }

}
