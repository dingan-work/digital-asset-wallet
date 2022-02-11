package wiki.ganhua.wallet.web3j.config;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ganhua
 * @date 2022/2/9
 */
@Data
public class Web3jDefaultConfig implements Serializable {

    private static final long serialVersionUID = -7911762880759414013L;

    protected transient ChainType eth = new ChainType(List.of("https://ethmainnet.pentoken.io"),1L,60);

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
