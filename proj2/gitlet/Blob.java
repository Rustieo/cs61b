package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    final String fileName;
    final String filePath;
    final String storeName;//Blob文件的名字,也是文件的SHA值
    final byte[]content;
    public Blob(File file){
        this.content=Utils.readContents(file);
        this.storeName=Utils.sha1(content);
        this.fileName=file.getName();
        this.filePath=file.getPath();
    }
    public void save(){
        File output=Utils.join(Repository.BLOBS,this.storeName);
        Utils.writeObject(output,this);
    }
}

