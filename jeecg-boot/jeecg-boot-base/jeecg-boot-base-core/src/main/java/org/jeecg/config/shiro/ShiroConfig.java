package org.jeecg.config.shiro;

import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.shiro.filters.CustomShiroFilterFactoryBean;
import org.jeecg.config.shiro.filters.JwtFilter;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.*;

/**
 * @author: Scott
 * @date: 2018/2/7
 * @description: shiro 配置类
 */

@Slf4j
@Configuration
public class ShiroConfig {

    @Value("${jeecg.shiro.excludeUrls}")
    private String excludeUrls;

    @Autowired
    private Environment env;


    /**
     * Filter Chain定义说明
     *
     * 1、一个URL可以配置多个Filter，使用逗号分隔
     * 2、当设置多个过滤器时，全部验证通过，才视为通过
     * 3、部分过滤器可指定参数，如perms，roles
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        if(oConvertUtils.isNotEmpty(excludeUrls)){
            String[] permissionUrl = excludeUrls.split(",");
            for(String url : permissionUrl){
                filterChainDefinitionMap.put(url,"anon");
            }
        }
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/sys/cas/client/validateLogin", "anon"); //cas验证登录
        filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); //登录验证码接口排除
        filterChainDefinitionMap.put("/sys/checkCaptcha", "anon"); //登录验证码接口排除
        filterChainDefinitionMap.put("/sys/login", "anon"); //登录接口排除
        filterChainDefinitionMap.put("/sys/mLogin", "anon"); //登录接口排除
        filterChainDefinitionMap.put("/sys/logout", "anon"); //登出接口排除
        filterChainDefinitionMap.put("/sys/thirdLogin/**", "anon"); //第三方登录
        filterChainDefinitionMap.put("/sys/getEncryptedString", "anon"); //获取加密串
        filterChainDefinitionMap.put("/sys/sms", "anon");//短信验证码
        filterChainDefinitionMap.put("/sys/phoneLogin", "anon");//手机登录
        filterChainDefinitionMap.put("/sys/user/checkOnlyUser", "anon");//校验用户是否存在
        filterChainDefinitionMap.put("/sys/user/register", "anon");//用户注册
        filterChainDefinitionMap.put("/sys/user/querySysUser", "anon");//根据手机号获取用户信息
        filterChainDefinitionMap.put("/sys/user/phoneVerification", "anon");//用户忘记密码验证手机号
        filterChainDefinitionMap.put("/sys/user/passwordChange", "anon");//用户更改密码
        filterChainDefinitionMap.put("/auth/2step-code", "anon");//登录验证码
        filterChainDefinitionMap.put("/sys/common/static/**", "anon");//图片预览 &下载文件不限制token
        filterChainDefinitionMap.put("/sys/common/pdf/**", "anon");//pdf预览
        filterChainDefinitionMap.put("/generic/**", "anon");//pdf预览需要文件
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/**/*.js", "anon");
        filterChainDefinitionMap.put("/**/*.css", "anon");
        filterChainDefinitionMap.put("/**/*.html", "anon");
        filterChainDefinitionMap.put("/**/*.svg", "anon");
        filterChainDefinitionMap.put("/**/*.pdf", "anon");
        filterChainDefinitionMap.put("/**/*.jpg", "anon");
        filterChainDefinitionMap.put("/**/*.png", "anon");
        filterChainDefinitionMap.put("/**/*.ico", "anon");

        filterChainDefinitionMap.put("/**/*.ttf", "anon");
        filterChainDefinitionMap.put("/**/*.woff", "anon");
        filterChainDefinitionMap.put("/**/*.woff2", "anon");

        filterChainDefinitionMap.put("/druid/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger**/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v2/**", "anon");

        //积木报表排除
        filterChainDefinitionMap.put("/jmreport/**", "anon");
        filterChainDefinitionMap.put("/**/*.js.map", "anon");
        filterChainDefinitionMap.put("/**/*.css.map", "anon");
        //大屏设计器排除
        filterChainDefinitionMap.put("/bigscreen/**", "anon");

        //测试示例
        filterChainDefinitionMap.put("/test/bigScreen/**", "anon"); //大屏模板例子
        //filterChainDefinitionMap.put("/test/jeecgDemo/rabbitMqClientTest/**", "anon"); //MQ测试
        //filterChainDefinitionMap.put("/test/jeecgDemo/html", "anon"); //模板页面
        //filterChainDefinitionMap.put("/test/jeecgDemo/redis/**", "anon"); //redis测试

        //websocket排除
        filterChainDefinitionMap.put("/websocket/**", "anon");//系统通知和公告
        filterChainDefinitionMap.put("/newsWebsocket/**", "anon");//CMS模块
        filterChainDefinitionMap.put("/vxeSocket/**", "anon");//JVxeTable无痕刷新示例
        filterChainDefinitionMap.put("/eoaSocket/**","anon");//我的聊天

        //性能监控  TODO 存在安全漏洞泄露TOEKN（durid连接池也有）
        filterChainDefinitionMap.put("/actuator/**", "anon");

        // 添加自己的过滤器并且取名为jwt
        Map<String, Filter> filterMap = new HashMap<String, Filter>(1);
        //如果cloudServer为空 则说明是单体 需要加载跨域配置
        Object cloudServer = env.getProperty(CommonConstant.CLOUD_SERVER_KEY);
        filterMap.put("jwt", new JwtFilter(cloudServer==null));
        shiroFilterFactoryBean.setFilters(filterMap);
        // <!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边
        filterChainDefinitionMap.put("/**", "jwt");

        // 未授权界面返回JSON
        shiroFilterFactoryBean.setUnauthorizedUrl("/sys/common/403");
        shiroFilterFactoryBean.setLoginUrl("/sys/common/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(ShiroRealm myRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myRealm);

        /*
         * 关闭shiro自带的session，详情见文档
         * http://shiro.apache.org/session-management.html#SessionManagement-
         * StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        //自定义缓存实现,使用redis
//        securityManager.setCacheManager(redisCacheManager());
        return securityManager;
    }

    /**
     * 下面的代码是添加注解支持
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        /**
         * 解决重复代理问题 github#994
         * 添加前缀判断 不匹配 任何Advisor
         */
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("_no_advisor");
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }


}
