package wiki.ganhua.wallet.solana.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wiki.ganhua.wallet.solana.model.PublicKey;

import java.util.List;

/**
 * @author Ganhua
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TransactionInstruction {

    private List<AccountMeta> keys;
    private PublicKey programId;
    private byte[] data;

}
