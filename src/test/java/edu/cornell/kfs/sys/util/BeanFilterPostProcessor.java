package edu.cornell.kfs.sys.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * This factory and registry post-processor implementation will automatically remove
 * all beans from the registry, except for those that are explicitly referenced
 * by the given whitelist.
 */
public class BeanFilterPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private Set<String> beanWhitelist;

    public BeanFilterPostProcessor() {
        this.beanWhitelist = new HashSet<>();
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Arrays.stream(registry.getBeanDefinitionNames())
                .filter(this::shouldFilterBeanDefinition)
                .forEach(registry::removeBeanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Do nothing.
    }

    protected boolean shouldFilterBeanDefinition(String beanName) {
        return !beanWhitelist.contains(beanName);
    }

    public void setBeanWhitelist(Set<String> beanWhitelist) {
        this.beanWhitelist = beanWhitelist;
    }

}
