package com.samic.samic;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.samic.samic.config.ORCLConfig;
import jakarta.persistence.EntityListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
//@Import(TestContainerConfiguration.class)
@ComponentScan(basePackages = {"com.samic.samic.data.repositories"})
@Configuration
@EnableTransactionManagement
@EntityListeners(AuditingEntityListener.class)
@Import(ORCLConfig.class)
public class TestApplication {

    @Bean
    @RestartScope
    @ServiceConnection
    OracleContainer oracleContainer(){
        final int exposedPort = 1521;
        final int localPort = 1521;
        return new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21"))
                       .withExposedPorts(exposedPort)
                       .withCreateContainerCmdModifier(cmd ->{
                           cmd.withName("samic-oracle");
                           cmd.withHostConfig(new HostConfig()
                                                      .withPortBindings(new PortBinding(Ports.Binding.bindPort(1521), new ExposedPort(1521))));
                       });
    }

    public static void main(String[] args){
        SpringApplication.from(Application::main)
                .with(TestApplication.class)
                .run(args);
    }
}
