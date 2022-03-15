package wiki.ganhua.wallet.arweave.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wiki.ganhua.wallet.arweave.util.CryptoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ganhua
 * @date 2022/3/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    private String name;

    private String value;

    public static List<Tag> tagsEncode(List<Tag> tags){
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            Tag t = new Tag();
            t.name = CryptoUtils.encode(tag.getName());
            t.value = CryptoUtils.encode(tag.getValue());
            result.add(t);
        }
        return result;
    }


    public static List<Tag> tagsDecode(List<Tag> tags){
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            Tag t = new Tag();
            t.name = new String(CryptoUtils.decode(tag.getName()));
            t.value = new String(CryptoUtils.decode(tag.getValue()));
            result.add(t);
        }
        return result;
    }

    /**
     * 组合签名类型
     */
    public static String[][] tagValue(List<Tag> tags){
        String[][] tagVs = new String[tags.size()][2];
        for (int i = 0;i<tags.size();i++) {
            String[] v = new String[2];
            v[0] = tags.get(i).getName();
            v[1] = tags.get(i).getValue();
            tagVs[i] = v;
        }
        return tagVs;
    }

}
