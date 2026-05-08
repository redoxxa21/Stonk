package io.stonk.sim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=127.0.0.1:19099",
        "spring.kafka.listener.auto-startup=false",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class SimulationApplicationTests {

    @Test
    void contextLoads() {
    }
}
