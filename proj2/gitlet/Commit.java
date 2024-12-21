package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    public String message;
    public String timeLabel;
    public ArrayList<String> parents;/*父结点的类型不能是Commit,不然序列化时会重复储存;
    parents[0]是first parent,即执行merge命令时当前所在的分支*/
    public String ID;
    //public List<String>parentSHAs;
    //public List<String> files;
    Map<String,String>blobToFile;//文件路径与相应的blob对象名,文件路径为键,blob的名字为值
    public Commit(boolean flag){//最开始的commit对象的初始化
        this.timeLabel=new Date().toString();
        this.message="initial commit";
        //this.parentSHAs=new ArrayList<>();
        this.blobToFile=new TreeMap<>();
        this.ID=Utils.sha1(timeLabel,message);
        this.parents=new ArrayList<>();
        save();
    }
    public Commit(String message,String ...parent){
        this.timeLabel=new Date().toString();
        this.parents=new ArrayList<>();
        if(parent.length==1)this.parents.add(parent[0]);
        else {
            this.parents.add(parent[0]);
            this.parents.add(parent[1]);
        }
        this.message=message;
        extendParent();//继承父结点的成员变量(这里要用深拷贝!!!)
        scanAddArea();
        scanRemoveArea();
        createID();
        save();
    }
    private void scanRemoveArea(){//这里不涉及删除文件,删除文件的逻辑是在rm命令里的
        List<String >rmFileNames=Utils.plainFilenamesIn(Repository.REMOVE_AREA);//文件名是要删除的fileName,文件内容是Path
        for (int i = 0; i < rmFileNames.size(); i++) {
            File readPath=Utils.join(Repository.REMOVE_AREA,rmFileNames.get(i));
            String path=Utils.readObject(readPath,String.class);
            blobToFile.remove(path);
        }
    }
    private void scanAddArea(){//如果只把前三位作为文件夹名,查找时会方便得多,后面可以再修改/////////////
        /*List<String> blobSHAs=Utils.plainFilenamesIn(Repository.ADD_AREA);//这个是记录暂存区添加的文件的路径
        for (int i = 0; i < blobSHAs.size(); i++) {
            File target=Utils.join(Repository.ADD_AREA,blobSHAs.get(i));
            Blob blob=Utils.readObject(target, Blob.class);
            String filePath=blob.filePath;
            blobToFile.put(filePath,blobSHAs.get(i));
            blob.save();
        }*/
        HashMap<String,String>fileMap=Utils.readObject(Repository.STAGE,HashMap.class);//读取暂存表
        for(Map.Entry<String,String> entry:fileMap.entrySet()){
            String filePath=Utils.getFilePath(entry.getKey());
            blobToFile.put(filePath,entry.getValue());
            File target=Utils.join(Repository.ADD_AREA,entry.getValue());
            /*将blob对象名与文件名的映射添加到commit中.这里不需要判断版本库中是否有历史记录,因为map的映射
            是唯一的,新加的值会覆盖*/
            Blob blob=Utils.readObject(target, Blob.class);
            blob.save();//将blob保存

        }
        Utils.deleteDirFiles(Repository.ADD_AREA);
        fileMap=new HashMap<>();//清空暂存表
        Utils.writeObject(Repository.STAGE,fileMap);
    }
    private void extendParent(){
        File target=Utils.join(Repository.COMMITS,parents.get(0));
        Commit parentCommit= Utils.readObject(target,Commit.class);//利用父结点的SHA1获取父结点
        this.blobToFile=new TreeMap<>();
        blobToFile.putAll(parentCommit.blobToFile);
    }

    private void createID(){
        byte[]blobs=Utils.serialize((Serializable) this.blobToFile);/////////不知道这里的强转会不会有bug
        byte[]parentsByte=Utils.serialize(parents);
        this.ID=Utils.sha1(blobs,timeLabel,message,parentsByte);
    }
    /*public boolean containsFile(String fileName){
        return files.contains(fileName);
    }*/
    public boolean containsFilePath(String path){
        return blobToFile.containsKey(path);
    }

    /*
     输入文件路径,返回Blob对象
     */
    public Blob getBlob(String filePath){
        String blobSHA=this.blobToFile.get(filePath);
        File readBlob=Utils.join(Repository.BLOBS,blobSHA);
        return Utils.readObject(readBlob, Blob.class);
    }
    private void save(){
        File output=Utils.join(Repository.COMMITS,this.ID);
        Utils.writeObject(output,this);
    }

    /* TODO: fill in the rest of this class. */
}
