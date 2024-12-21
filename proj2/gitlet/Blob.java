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
        this.fileName=file.getName();
        this.storeName=Utils.sha1(fileName,content);//不能只用content,不然会出现两个名字不同的空文件sha1一样
        this.filePath=file.getPath();
    }
    public void save(){
        File output=Utils.join(Repository.BLOBS,this.storeName);
        Utils.writeObject(output,this);
    }
}

