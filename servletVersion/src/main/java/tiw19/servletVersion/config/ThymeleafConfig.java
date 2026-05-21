package tiw19.servletVersion.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@WebListener
public class ThymeleafConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();

        // set Thymeleaf paths
        String webappPath = context.getRealPath("/");
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(webappPath);
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        // Store Engine in ServletContext
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        context.setAttribute("templateEngine", engine);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) { /* Nothing to clean up */ }
}