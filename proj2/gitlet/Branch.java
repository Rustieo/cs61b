package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*public class Branch implements Serializable {
    List<String> commits;//这里用列表是因为,一个图中的一个分支必定是一个链表,多个链表(也就是分支)共同组成图
    String branchName;//我觉得这里不用SHA1,因为branch是唯一的,不需要多个版本
    public Branch(String branchName,String commitID){//master分支的构造方法
        this.branchName=branchName;
        this.commits=new ArrayList<>();
        this.commits.add(commitID);
    }
    public Branch(String branchName){
        this.branchName=branchName;
        this.commits=new ArrayList<>();
        //继承当前分支的commits
        Branch curBranch=Repository.getCurrentBranch();
        this.commits.addAll(curBranch.commits);
    }
    *//*获取最近一次Commit对象的SHA值*//*
    public String getLatestComSHA(){
        return this.commits.get(commits.size()-1);
    }
    *//*获取最近一次的Commit对象*//*
    public Commit getLatestCommit(){
           String SHA=this.getLatestComSHA();
           File readCommit=Utils.join(Repository.COMMITS,SHA);
           return Utils.readObject(readCommit, Commit.class);
    }
    public void save(){
        File output=Utils.join(Repository.BRANCHES,this.branchName);
    }

}*/
