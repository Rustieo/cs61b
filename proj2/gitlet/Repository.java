package gitlet;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /*Project Structure
    * /.gitlet
    *   /StageArea
    *       /Add_Area
    *       /Remo_Area
    *   /.objects
    *       /commits    存放commit对象,以SHA1命名
    *       /blobs      存放文件的Blob对象,以SHA1命名,注意Blob对象中的contents数组才等于文件内容,不能直接把blob对象写入文件
    *       /branches   存放分支,文件以分支名命名.内容是一个String字符串,为当前分支最新一次Commit的SHA值
    *       /HEAD       存放HEAD指针对象,只有一个文件,以HEAD命名(注意HEAD是文件不是文件夹!)
    * */
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGE_AREA=join(GITLET_DIR,"StageArea");
    public static final File OBJECTS=join(GITLET_DIR,".objects");
    public static final File BLOBS=join(OBJECTS,"blobs");
    public static final File COMMITS=join(OBJECTS,"commits");
    public static final File BRANCHES=join(OBJECTS,"branches");
    public static final File HEAD=join(OBJECTS,"HEAD");
    //public static final File CURRENT_BRANCH=join(OBJECTS,"current_branch");
    public static final File REMOVE_AREA=Utils.join(STAGE_AREA,"Remove_Area");
    public static final File ADD_AREA=Utils.join(STAGE_AREA,"Add_Area");
    //有一个改进方案:可以为每一个分支单独创建一个指向最新commit的指针,这样子就不用每次add的时候读取Branch了
    //还有一个:只取SHA1的前五位
    //对文件操作时没有判断是文件还是文件夹
    //执行命令时没有检查git仓库是否存在
    //可能的bug:文件的"\\"与"\";


    /* TODO: fill in the rest of this class. */
    public static void init(){//还需要判断仓库是否已存在
        if(GITLET_DIR.exists()&&GITLET_DIR.isDirectory()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        setPersistence();
        Commit initialCommit=new Commit(true);
        String initialBranch=initialCommit.ID;
        File output=join(BRANCHES,"master");
        Utils.writeObject(output,initialBranch);
        //Branch master=new Branch("master",initialCommit.ID);
        String head="master";
        Utils.writeObject(HEAD,head);
    }
    public static void setPersistence(){
        GITLET_DIR.mkdir();
        STAGE_AREA.mkdir();
        OBJECTS.mkdir();
        BLOBS.mkdir();
        COMMITS.mkdir();
        BRANCHES.mkdir();
        HEAD.mkdir();
        //CURRENT_BRANCH.mkdir();
        REMOVE_AREA.mkdir();
        ADD_AREA.mkdir();
    }

    //为什么会有这个方法:因为可能有git add par/son.txt
    public static File stringToFile(String name){
        return Utils.join(CWD,name);
    }
    public static boolean checkGit(){
        //To Do:
        return true;
    }

    public static Commit getCurCommit(){
        String branchName=Utils.readObject(HEAD, String.class);
        return getAnyCommit(branchName);
    }//获取当前分支的最新Commit
    public static Commit getAnyCommit(String branchName){
        File readBranch=join(BRANCHES,branchName);
        String commitID=Utils.readObject(readBranch, String.class);
        File readCommit=join(COMMITS,commitID);
        return readObject(readCommit, Commit.class);
    }//获取某一分支最新的Commit
    public static String getCurrentBranch(){////////////这里是不是可以把curBranch设为全局变量好点
        String head=Utils.readObject(HEAD,String.class);//获取当前分支
        File readBranch=Utils.join(BRANCHES,head);
        return Utils.readObject(readBranch,String.class);
    }
    /*1.文件先修改,add后再修改回上次commit的样子,再add
    2.文件add后要覆盖
     */
    public static void add(String fileName){//还要先判断是否是文件,还要判断是否与版本库中的重合
        File file=stringToFile(fileName);
        if(!file.isFile()){
            System.out.println("File does not exist.");
            return;
        }
        String hash=Utils.sha1(file);
        Commit commit=getCurCommit();//读取当前分支的最新Commit
        if(commit.blobToFile.containsKey(file.getPath())){
            if (commit.blobToFile.get(file.getPath()).equals(hash)){
                File isTheSameFile=Utils.join(ADD_AREA,hash);
                if(isTheSameFile.exists()){//判断暂存区是否有该文件
                    isTheSameFile.delete();
                }//假如文件add后与commit相同,且暂存区已有该文件,则把该文件移出暂存区
                return;//代表文件与最新一次commit中的一样
            }
        }
        Blob blob=new Blob(file);
        File stagedBlob=Utils.join(ADD_AREA,blob.storeName);/////////////
        Utils.writeObject(stagedBlob,blob);
    }
    public static void commit(String message){
        String parCommitSHA=getCurrentBranch();//读取父结点的SHA
        Commit commit=new Commit(parCommitSHA,message);
    }
    /*先检查文件是否被跟踪
    若暂存区有文件,则移出
      再把文件名储存下来,之后删除文件
    */
    public static void rm(String fileName){
        File file=stringToFile(fileName);
        String hash=Utils.sha1(file);
        String path=file.getPath();
        String name=file.getName();
        //检查是否在暂存区
        File isStaged=Utils.join(ADD_AREA,hash);
        //检查是否被跟踪
        Commit currentCommit=getCurCommit();
        if(!currentCommit.containsFilePath(path)){
            //如果未被跟踪,检查暂存区是否有文件
            if(isStaged.exists()){
                isStaged.delete();
                return;
            }
            System.out.println("No reason to remove the file.");
            return;
        }
        //如果被跟踪,检查暂存区的文件
        if(isStaged.exists()){
            isStaged.delete();
        }
        File output=Utils.join(REMOVE_AREA,name);//这里储存的文件的名字是要删除的file的name,但文件内容是filePath
        Utils.writeObject(output,path);
        //删除文件
        file.delete();
    }
    //获取文件在头提交中的版本，并将其放入工作目录，如果文件已经存在，则覆盖其版本。文件的新版本不会被暂存。
    public static void checkout1(String fileName){
        File file=stringToFile(fileName);//获取当前文件的文件对象(工作目录中)
        Commit latestCom=getCurCommit();
        //如果文件没有被追踪,则报错并返回
        if(!latestCom.containsFilePath(file.getPath())){
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob blob=latestCom.getBlob(file.getPath());
        Utils.writeContents(file,blob.content);//覆盖文件
    }
    /*获取文件在具有给定 ID 的提交中的版本，并将其放入工作目录，如果文件已经存在，则覆盖其版本。文件的新版本不会被暂存。
    命令:java gitlet.Main checkout [commit id] -- [file name]*/
    public static void checkout2(String commitID,String fileName){
        File file=stringToFile(fileName);//获取当前文件的文件对象(工作目录中)
        File readCommit=Utils.join(COMMITS,commitID);
        //判断该commit是否存在
        if(!readCommit.exists()){
            System.out.println("No commit with that id exists.");
        }
        Commit commit=Utils.readObject(readCommit, Commit.class);
        //判断文件是否被该commit追踪
        if(!commit.containsFile(file.getPath())){
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blob blob=commit.getBlob(file.getPath());
        Utils.writeContents(file,blob.content);//覆盖文件

    }
    /*3.获取给定分支头提交中的所有文件，并将它们放入工作目录，如果文件已经存在，则覆盖其版本。
    此外，在执行此命令后，给定分支将被视为当前分支（HEAD）。当前分支中跟踪但在检出的分支中不存在的任何文件都将被删除。
    除非检出的分支是当前分支，否则暂存区将被清空（见下面的失败情况）。
    命令:java gitlet.Main checkout [branch name]
    */
    public static void checkout3(String branchName){
        File readNewBranch=Utils.join(BRANCHES,branchName);//检查要切换到的分致是否存在
        if(!readNewBranch.exists()){
            System.out.println("No such branch exists.");
            return;
        }
        String curBranchName=Utils.readObject(HEAD, String.class);//检查当前分支是否是要切换到的分支
        if(curBranchName.equals(branchName)){
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit curCommit=getCurCommit();
        Commit newCommit=getAnyCommit(branchName);
        checkUntrackedFiles(curCommit,newCommit);
        setBranch(branchName);
        Utils.deleteDirContent(STAGE_AREA);//清空暂存区
    }
    public static void checkUntrackedFiles(Commit curCommit,Commit newCommit){
        for(Map.Entry<String,String> entry:newCommit.blobToFile.entrySet()){
            if(!curCommit.blobToFile.containsKey(entry.getKey())){//如果新分支有原分支没有追踪的文件
                File notTrackedFile=Utils.join(entry.getKey());
                if(notTrackedFile.exists()){//如果这个文件存在,则报错
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return ;
                }
            }
            File readBlob=Utils.join(BLOBS,entry.getValue());
            Blob blob=Utils.readObject(readBlob, Blob.class);
            File output=Utils.join(entry.getKey());//这里是不是会存在"\"与"\\"的问题
            Utils.writeObject(output,blob.content);
        }
    }
    public static void setBranch(String branchName){
        Utils.writeObject(HEAD,branchName);
    }
    public static void branch(String branchName){
        List<String>branches=Utils.plainFilenamesIn(BRANCHES);
        if(branches.contains(branchName)){
            System.out.println("A branch with that name already exists.");
        }
        Utils.writeObject(BRANCHES,branchName);
        setBranch(branchName);
    }
    public static void reset(String commitID){
        File readCommit=Utils.join(COMMITS,commitID);
        //判断该commit是否存在
        if(!readCommit.exists()){
            System.out.println("No commit with that id exists.");
        }
        Commit newCommit=Utils.readObject(readCommit, Commit.class);
        Commit curCommit=getCurCommit();
        checkUntrackedFiles(curCommit,newCommit);
        String head=Utils.readObject(HEAD,String.class);//获取当前分支
        File readBranch=Utils.join(BRANCHES,head);
        Utils.writeObject(readBranch,commitID);//移动当前分支的HEAD指针
        Utils.deleteDirContent(STAGE_AREA);//清空暂存区
    }
    public static void rmBranch(String branchName){
        File readBranch=Utils.join(BRANCHES,branchName);//检查要删除的分支是否存在
        if(!readBranch.exists()){
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String curBranchName=getCurrentBranch();//检查当前分支是否是要切换到的分支
        if(curBranchName.equals(branchName)){
            System.out.println("Cannot remove the current branch.");
            return;
        }
        readBranch.delete();
    }
    public static void merge(String branchName){
        //错误情况记得写
        Commit curCommit=getCurCommit();//curCommit即为HEAD
        Commit mergeCommit=getAnyCommit(branchName);
        Commit spiltCommit=findSpiltPoint(curCommit,mergeCommit);
    }
    private static void mergeBranch(Commit curCommit,Commit mergeCommit,Commit spiltCommit){
        for(Map.Entry<String,String> entry:mergeCommit.blobToFile.entrySet()){
            String filePath=entry.getKey();
            if(!curCommit.blobToFile.containsKey(filePath)){//如果新分支有原分支没有追踪的文件
                File notTrackedFile=Utils.join(filePath);
                if(notTrackedFile.exists()){//如果这个文件存在,则报错
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return ;
                }
            }else {
                if(!curCommit.blobToFile.get(filePath).equals(entry.getValue())){//如果该文件在两个commit中的版本不同

                }
            }
            /*File readBlob=Utils.join(BLOBS,entry.getValue());
            Blob blob=Utils.readObject(readBlob, Blob.class);
            File output=Utils.join(entry.getKey());//这里是不是会存在"\"与"\\"的问题
            Utils.writeObject(output,blob.content);*/
        }
    }
    private static Commit findSpiltPoint(Commit curCommit,Commit mergeCommit){
        List<String>headList=curCommit.parentSHAs;
        List<String>mergeList=mergeCommit.parentSHAs;
        /*遍历两个commit的列表,当第一次出现不一样的commit时即为spilt point*/
        for (int i = 1; i < headList.size(); i++) {//循环从i=1开始
            if(!headList.get(i).equals(mergeList.get(i))){
                File out=Utils.join(COMMITS,headList.get(i-1));
                return readObject(out, Commit.class);
            }
        }
        return null;
    }
}
