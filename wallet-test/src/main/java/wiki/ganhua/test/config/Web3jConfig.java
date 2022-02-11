package wiki.ganhua.test.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Ganhua
 * @date 2022/2/11
 */

@Component
@PropertySource(value = "classpath:web3j-info.yml")
@ConfigurationProperties(prefix = "web3j")
@Data
public class Web3jConfig {

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
         * https://github.com/satoshilabs/slips/blob/master/slip-0044.md
         */
        private int childNumber;
    }

}
