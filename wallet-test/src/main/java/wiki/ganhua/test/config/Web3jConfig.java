package wiki.ganhua.test.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

import static wiki.ganhua.test.config.Web3jConfig.WEB3J;

/**
 * @author Ganhua
 * @date 2022/2/11
 */

@Component
@PropertySource(value = "classpath:node-info.yml")
@ConfigurationProperties(prefix = WEB3J)
@Data
public class Web3jConfig {

    public static final String WEB3J = "web3j";

    private ChainType eth;

    private ChainType bnb;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ChainType{
        /**
         * rpcUrl
         */
        private List<String> rpcUrl;

        /**
         * 该链ID可通过 ethChainId 方法获取 避免失败 手动传入
         * 链ID
         */
        private Long chainId;

        /**
         * 创建钱包地址时的 BIP-0044 类型
         * <a href="https://github.com/satoshilabs/slips/blob/master/slip-0044.md">...</a>
         */
        private int childNumber;
    }

}
