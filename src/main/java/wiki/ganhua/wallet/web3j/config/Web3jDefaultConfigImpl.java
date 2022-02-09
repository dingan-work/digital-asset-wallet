package wiki.ganhua.wallet.web3j.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Ganhua
 * @date 2022/2/9
 */
public class Web3jDefaultConfigImpl implements Serializable {

    private static final long serialVersionUID = -7911762880759414013L;

    protected volatile ChainType eth;



    @Data
    private static class ChainType{
        /**
         * rpcUrl
         */
        private String rpcUrl;

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
