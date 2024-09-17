package gitlet;

import java.io.File;
import java.io.Serializable;

public class Head implements Serializable {
    public String currentBranch;//代表当前所指向的分支
    public Head(String branch){
        this.currentBranch=branch;
    }
    public void save(){
        File output=Utils.join(Repository.HEAD,"HEAD");//这里不能填分支名,因为你先读取HEAD对象才知道当前分支名
        Utils.writeObject(output,this);
    }
}
