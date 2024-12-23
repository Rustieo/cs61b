package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    public static void main(String[] args) {
        //File tar=Utils.join("D:\\gitletTest","man.txt");
        File tar=new File("D:\\ha");
        List<String>list=plainFilenamesIn(tar);
        for (String s:list){
            System.out.println(s);
        }
    }
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /*Project Structure
    * /.gitlet
    *   /StageArea 暂存区
    *       /Add_Area  存放add
    *       /Remove_Area  存放remove
    *       /STAGE     存放stage对象,stage对象储存"add_area的文件名-文件sha值对照表"
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
    public static final File REMOVE_AREA=Utils.join(STAGE_AREA,"Remove_Area");
    public static final File ADD_AREA=Utils.join(STAGE_AREA,"Add_Area");
    public static final File STAGE=Utils.join(STAGE_AREA,"STAGE_MAP");
    //有一个改进方案:可以为每一个分支单独创建一个指向最新commit的指针,这样子就不用每次add的时候读取Branch了
    //还有一个:只取SHA1的前五位
    //对文件操作时没有判断是文件还是文件夹
    //执行命令时没有检查git仓库是否存在
    //可能的bug:文件的"\\"与"\";
    //同一文件add两次
    //"Incorrect operands."这个failure case没有处理
    /* TODO: fill in the rest of this class. */

    public static void init() throws IOException {//还需要判断仓库是否已存在
        if(checkGit()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        setPersistence();
        Commit initialCommit=new Commit(true);
        String initialBranch=initialCommit.ID;
        File output=join(BRANCHES,"master");
        Utils.writeObject(output,initialBranch);
        String head="master";
        Utils.writeObject(HEAD,head);
    }
    public static void setPersistence() throws IOException {
        GITLET_DIR.mkdir();
        STAGE_AREA.mkdir();
        OBJECTS.mkdir();
        BLOBS.mkdir();
        COMMITS.mkdir();
        BRANCHES.mkdir();
        HEAD.createNewFile();
        //CURRENT_BRANCH.mkdir();
        REMOVE_AREA.mkdir();
        ADD_AREA.mkdir();
        HashMap<String,String>nameToBlob=new HashMap<>();//////////
        Utils.writeObject(STAGE,nameToBlob);
    }

    public static boolean checkGit(){
        /*不知道为什么大家的代码都是检查cwd下是否有.gitlet,而不考虑子文件夹的情况?可能项目简化了?*/
        return GITLET_DIR.isDirectory()&&GITLET_DIR.exists();
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
    public static String getCurBranchCommitID(){
        String head=Utils.readObject(HEAD,String.class);//获取当前分支的最新commitID
        File readBranch=Utils.join(BRANCHES,head);
        return Utils.readObject(readBranch,String.class);
    }
    public static String getCurrentBranch(){//返回当前分支的名字,配合join方法可以获取当前分支的文件路径
        return Utils.readObject(HEAD, String.class);
    }
    /*1.文件先修改,add后再修改回上次commit的样子,再add
    2.文件add后要覆盖
     */
    public static void add(String fileName){//还要先判断是否是文件,还要判断是否与版本库中的重合
        File file=nameToFile(fileName);
        if(!file.isFile()){
            System.out.println("File does not exist.");
            return;
        }
        File checkIfRemoved=Utils.join(REMOVE_AREA,fileName);
        if(checkIfRemoved.exists())checkIfRemoved.delete();//如果该文件在删除区,则移出
        HashMap<String,String> fileMap=Utils.readObject(STAGE,HashMap.class);
        Blob blob=new Blob(file);
        String hash=blob.storeName;
        Commit commit=getCurCommit();//读取当前分支的最新Commit
        if(commit.blobToFile.getOrDefault(file.getPath(),"null").equals(hash)){
            fileMap.remove(fileName);
            //System.out.println("File is unchanged");////////////这是我自己编的提示,文档中没要求
            //假如文件当前版本与最新commit中的版本相同,此次add无效.并且如果暂存区中已经有该文件了,则把该文件移出暂存区
            return;
        }

        File stagedBlob=Utils.join(ADD_AREA,blob.storeName);/////////////
        Utils.writeObject(stagedBlob,blob);
        ///////////////
        fileMap.put(fileName,hash);
        Utils.writeObject(STAGE,fileMap);
    }
    public static void commit(String message){
        if(message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        int addLen=ADD_AREA.listFiles().length;
        int rmLen=REMOVE_AREA.listFiles().length;
        if(addLen==0&&rmLen==0){
            System.out.println("No changes added to the commit.");
            return;
        }
        String parCommitSHA=getCurBranchCommitID();//读取父结点的SHA
        Commit commit=new Commit(message,parCommitSHA);//创建commit对象
        String curBranch=getCurrentBranch();//获取当前分支名字
        File output=Utils.join(BRANCHES,curBranch);//获取当前分支路径
        Utils.writeObject(output,commit.ID);//让当前分支指向这个commit,其他分支指针不移动
    }
    /*如果文件被暂存,则单纯移出暂存区,不做其他改动.如果没有被暂存,检查是够被跟踪,如果跟踪则删除工作区中的文件并取消跟踪.
       如果既没有被暂存,也没有被跟踪,则不做其他改动
    */
    public static void rm(String fileName) {
        File file = nameToFile(fileName);
        String path = file.getPath();
        String name = file.getName();
        Commit currentCommit = getCurCommit();
        //检查是否在暂存区\
        HashMap<String, String> fileMap = Utils.readObject(STAGE, HashMap.class);
        if (fileMap.containsKey(fileName)) {
            String add_StageHash = fileMap.get(fileName);
            File addFile = Utils.join(ADD_AREA, add_StageHash);
            addFile.delete();
            fileMap.remove(fileName);
            Utils.writeObject(STAGE, fileMap);//持久化暂存表
        } else if (currentCommit.containsFilePath(path)) {
            if(file.exists()) file.delete();
            File output = Utils.join(REMOVE_AREA, name);//这里储存的文件的名字是要删除的file的name,但文件内容是filePath
            Utils.writeObject(output, path);
        }  else {
            System.out.println("No reason to remove the file.");
        }
    }
    public static void log(){
        Commit commit=getCurCommit();
        while(!commit.parents.isEmpty()){
            printCommit(commit);
            String parent=commit.parents.get(0);
            File readCommit=Utils.join(COMMITS,parent);
            commit= readObject(readCommit, Commit.class);
        }
        printCommit(commit);
    }
    private static void printCommit(Commit commit){
        System.out.println("===");
        System.out.println("commit " + commit.ID);
        if (commit.parents.size() > 1) {
            String print = "Merge: " + commit.parents.get(0).substring(0, 7)
                    + " " + commit.parents.get(1).substring(0, 7);
            System.out.println(print);
        }
        System.out.println("Date: " + commit.timeLabel);
        System.out.println(commit.message);
        System.out.println();
    }

    public static void globalLog(){
        List<String>list=plainFilenamesIn(COMMITS);
        for (String s:list){
            File readCommit=Utils.join(COMMITS,s);
            printCommit(Utils.readObject(readCommit, Commit.class));
        }
    }

    public static void find(String message){
        List<String>list=plainFilenamesIn(COMMITS);
        int count=0;
        for (String s:list){
            File readCommit=Utils.join(COMMITS,s);
            Commit commit=readObject(readCommit, Commit.class);
            if(commit.message.equals(message)) {
                System.out.println(commit.ID);
                count++;
            }
        }
        if(count==0){
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status(){
        System.out.println("=== Branches ===");
        listBranch();
        System.out.println("\n=== Staged Files ===");
        listStagedFiles();//这个用不用打印在暂存区但是" Modifications Not Staged For Commit"的文件
        System.out.println("\n=== Removed Files ===");
        listRemovedFiles();
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        listModified();
        System.out.println("\n=== Untracked Files ===");
        //listUntracked();
    }
    private static void listBranch(){
        List<String> list=plainFilenamesIn(BRANCHES);///////////当前分支用不用第一个打印?
        String curBranch=getCurrentBranch();
        for(String s:list){
            if(s.equals(curBranch)) System.out.println("*"+s);
            else System.out.println(s);
        }
    }
    private static void listStagedFiles(){
        HashMap<String,String>fileMap=readObject(STAGE,HashMap.class);
        for (Map.Entry entry:fileMap.entrySet()){
            System.out.println(entry.getKey());
        }
    }
    private static void listRemovedFiles(){
        List<String> list=plainFilenamesIn(REMOVE_AREA);
        for(String s:list){
            System.out.println(s);
        }
    }
    private static void listModified(){
        HashMap<String,String>fileMap=readObject(STAGE,HashMap.class);
        for (HashMap.Entry <String,String>entry:fileMap.entrySet()){
            String fileName=entry.getKey();
            File file=new File(getFilePath(fileName));
            if(!file.exists()){//如果文件在暂存区中但在工作区中被删除
                System.out.println(fileName);
                continue;
            }
            byte[]content=readContents(file);
            String shaInCWD=sha1(fileName,content);
            if(!shaInCWD.equals(entry.getValue())){//如果文件在暂存区中,但和工作区中的版本不同
                System.out.println(fileName);
                continue;
            }
        }
        Commit commit=getCurCommit();
        for (Map.Entry<String,String>entry:commit.blobToFile.entrySet()){
            String fileName=getFileName(entry.getKey());
            if(fileMap.containsKey(fileName))continue;//在暂存区的情况上面已经考虑过了
            File file=new File(entry.getKey());
            if(!file.exists()){
                List<String>rmList=plainFilenamesIn(REMOVE_AREA);
                if(!rmList.contains(fileName)) System.out.println(fileName);//如果文件不存在且不在删除区域中
                continue;
            }
            byte[]content=readContents(file);
            String shaInCWD=sha1(fileName,content);
            if(!shaInCWD.equals(entry.getValue())){//如果文件不在暂存中,且在工作区中的版本和最新commit中的不同
                System.out.println(fileName);
            }
        }
    }
    /*private static void listUntracked(){
        List<String>fileList=plainFilenamesIn(CWD);
        HashMap<String,String>fileMap=readObject(STAGE,HashMap.class);
        Commit commit=getCurCommit();
        for (String fileName:fileList){//既不在暂存区,也没有被追踪
            if(!fileMap.containsKey(fileName)&&!commit.blobToFile.containsKey(getFilePath(fileName))){
                System.out.println(fileName);
            }
        }
    }*/

    /*获取文件在头提交中的版本，并将其放入工作目录，如果文件已经存在，则覆盖其版本。文件的新版本不会被暂存。*/
    public static void checkout1(String fileName){
        File file=nameToFile(fileName);//获取当前文件的文件对象(工作目录中)
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
        File file=nameToFile(fileName);//获取当前文件的文件对象(工作目录中)
        File readCommit=Utils.join(COMMITS,commitID);
        //判断该commit是否存在
        if(!readCommit.exists()){
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit=Utils.readObject(readCommit, Commit.class);
        //判断文件是否被该commit追踪
        if(!commit.containsFilePath(file.getPath())){
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
        dealFiles(curCommit,newCommit);
        setBranch(branchName);
        Utils.deleteDirContent(ADD_AREA);//清空暂存区
        Utils.deleteDirContent(REMOVE_AREA);//清空暂存区
    }
    /* public static void checkUntrackedFiles(Commit curCommit,Commit newCommit){
        //如果有文件在当前分支未被追踪,且该文件在新分支中被追踪,则报错
        List<String> fileList=Utils.plainFilenamesIn(CWD);
        for(String s:fileList){
            File file=Utils.join(CWD,s);
            String filePath=file.getPath();
            //String filePath= CWD.getPath()+File.separator+s;这种方法开销小点,但我怕有bug
            if(!curCommit.blobToFile.containsKey(filePath)){
                if(newCommit.blobToFile.containsKey(filePath)){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return ;
                }
            }
        }
        for(Map.Entry<String,String> entry:newCommit.blobToFile.entrySet()){
            if(!curCommit.blobToFile.containsKey(entry.getKey())){//如果新分支有原分支没有追踪的文件
                File notTrackedFile=Utils.join(entry.getKey());
                if(notTrackedFile.exists()){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return ;
                }
            }
            File readBlob=Utils.join(BLOBS,entry.getValue());
            Blob blob=Utils.readObject(readBlob, Blob.class);
            File output=Utils.join(entry.getKey());//这里是不是会存在"\"与"\\"的问题
            Utils.writeContents(output,blob.content);
        }
    }*/
    private static void dealFiles(Commit curCommit,Commit newCommit){
        checkUntrackedFiles(curCommit,newCommit);
        for(Map.Entry<String,String>entry:curCommit.blobToFile.entrySet()){
            String filePath=entry.getKey();
            File file=new File(filePath);
            file.delete();
        }//删除curCommit追踪的文件
        for(Map.Entry<String,String> entry:newCommit.blobToFile.entrySet()){
            String filePath=entry.getKey();
            Blob fileBlob=newCommit.getBlob(filePath);
            File file=new File(filePath);
            Utils.writeContents(file,fileBlob.content);
        }//把newCommit的文件写上去
    }
    private static void checkUntrackedFiles(Commit curCommit,Commit newCommit){
        for(Map.Entry<String,String> entry:newCommit.blobToFile.entrySet()){
            String filePath=entry.getKey();
            File file=new File(filePath);
            if(!curCommit.blobToFile.containsKey(filePath)&&file.exists()){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }//找出newCom追踪但curCom没追踪且在当前工作区存在的文件,返回true说明有未追踪且在工作区存在的文件
    public static void setBranch(String branchName){
        Utils.writeObject(HEAD,branchName);
    }//把HEAD指针移动到给定分支
    public static void branch(String branchName){
        List<String>branches=Utils.plainFilenamesIn(BRANCHES);
        if(branches.contains(branchName)){
            System.out.println("A branch with that name already exists.");
        }
        File output =Utils.join(BRANCHES,branchName);
        String curCommitID=getCurBranchCommitID();
        Utils.writeObject(output,curCommitID);
    }
    public static void reset(String commitID){
        File readCommit=Utils.join(COMMITS,commitID);
        //判断该commit是否存在
        if(!readCommit.exists()){
            System.out.println("No commit with that id exists.");
        }
        Commit newCommit=Utils.readObject(readCommit, Commit.class);
        Commit curCommit=getCurCommit();
        dealFiles(curCommit,newCommit);
        String head=Utils.readObject(HEAD,String.class);//获取当前分支
        File readBranch=Utils.join(BRANCHES,head);
        Utils.writeObject(readBranch,commitID);//移动当前分支的HEAD指针
        Utils.deleteDirContent(ADD_AREA);//清空暂存区
        Utils.deleteDirContent(REMOVE_AREA);//清空暂存区
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
        if(ADD_AREA.list().length!=0|| REMOVE_AREA.list().length!=0){
            System.out.println("You have uncommitted changes.");
            return;
        }
        if(branchName.equals(getCurrentBranch())){
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit curCommit=getCurCommit();//curCommit即为HEAD
        Commit newCommit=getAnyCommit(branchName);
        checkUntrackedFiles(curCommit,newCommit);
        Commit spiltCommit=findSpiltPoint(curCommit,newCommit);
        if(spiltCommit.ID.equals(curCommit.ID)){//如果分支点是curCom,可以快速合并
            String curBranch=getCurrentBranch();//获取当前分支名字
            File output=Utils.join(BRANCHES,curBranch);//获取当前分支路径
            Utils.writeObject(output,newCommit.ID);//让当前分支指向这个commit,其他分支指针不移动
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        mergeFiles(curCommit,newCommit,spiltCommit);
        String HEAD=getCurrentBranch();
        String mergeMessage="Merged "+branchName+" "+"into "+HEAD+".";
        mergeCommit(mergeMessage,curCommit.ID, newCommit.ID);
    }
    private static Commit findSpiltPoint(Commit curCommit,Commit newCommit){
        /*先bfs记录curCom到头提交的路径,然后在bfs newCom,一边遍历一边查找spilt point*/
        Set<String> path=new HashSet<>();//路径
        /*这个集合的泛型不能是commit对象(除非commit类重写了equals,因为不重写的话,它比较的时候会考虑内存地址,
        但每次从本地读取出来的对象的内存地址都是不一样的!!!)*/
        Deque<Commit>deque=new ArrayDeque<>();
        path.add(curCommit.ID);
        deque.add(curCommit);
        while(!deque.isEmpty()){
            Commit com=deque.poll();
            if(com.ID.equals(newCommit.ID)){
                System.out.println("Given branch is an ancestor of the current branch.");
                System.exit(0);
            }
            int parentNum=com.parents.size();
            if(parentNum==1){
                String parentSHA=com.parents.get(0);
                File tar=Utils.join(COMMITS,parentSHA);
                Commit parent=readObject(tar, Commit.class);
                if(!path.contains(parent.ID)){
                    path.add(parent.ID);
                    deque.add(parent);
                }

            }else if(parentNum==2){
                String parent1SHA=com.parents.get(0);
                String parent2SHA=com.parents.get(1);
                File tar1=Utils.join(COMMITS,parent1SHA);
                File tar2=Utils.join(COMMITS,parent2SHA);
                Commit parent1=readObject(tar1, Commit.class);
                Commit parent2=readObject(tar2, Commit.class);
                if(!path.contains(parent1.ID)){
                    path.add(parent1.ID);
                    deque.add(parent1);
                }
                if(!path.contains(parent2.ID)){
                    path.add(parent2.ID);
                    deque.add(parent2);
                }
            } else if (parentNum==0) {//遇到头提交了
                break;
            }
        }
        deque=new ArrayDeque<>();
        deque.add(newCommit);
        while(!deque.isEmpty()){
            Commit com=deque.poll();
            boolean flag=path.contains(com.ID);
            if(flag){
                return com;
            }
            int parentNum=com.parents.size();
            if(parentNum==1){
                String parentSHA=com.parents.get(0);
                File tar=Utils.join(COMMITS,parentSHA);
                Commit parent=readObject(tar, Commit.class);
                deque.add(parent);
            }else if(parentNum==2){
                String parent1SHA=com.parents.get(0);
                String parent2SHA=com.parents.get(1);
                File tar1=Utils.join(COMMITS,parent1SHA);
                File tar2=Utils.join(COMMITS,parent2SHA);
                Commit parent1=readObject(tar1, Commit.class);
                Commit parent2=readObject(tar2, Commit.class);
                deque.add(parent1);
                deque.add(parent2);
            } else if (parentNum==0) {//遇到头提交了
                return com;
            }
        }
        return null;
    }
    private static void mergeFiles(Commit curCommit,Commit newCommit,Commit spiltCommit){
        for(Map.Entry<String,String>entry:curCommit.blobToFile.entrySet()){//由于是遍历curCom,因此这个循环不考虑curBlob为null的情况
            String filePath=entry.getKey();
            String fileName=getFileName(filePath);
            String curBlob=entry.getValue();
            String newBlob=newCommit.blobToFile.getOrDefault(filePath,"null");
            String sptBlob=spiltCommit.blobToFile.getOrDefault(filePath,"null");
            if(curBlob.equals(newBlob)&&curBlob.equals(sptBlob)){//如果三者都相同,不变
                //mergeComBlobs.put(filePath,curBlob);
                //add(fileName);诶,你可能会问:注释掉和不注释掉不是一样吗?但如果文件有为暂存的更改,merge后工作区就变成为暂存的更改后的样子了而不是curCom中的样子
                mergeCheckout(curCommit,filePath);
            } else if (!curBlob.equals(newBlob)&&!curBlob.equals(sptBlob)&&!newBlob.equals(sptBlob)) {//三者互不相同,冲突
                dealConflict(filePath,curBlob,newBlob);
            } else if (curBlob.equals(newBlob)) {//当前分支和合并分支做了相同的改动,用这个相同的
                //mergeComBlobs.put(filePath,curBlob);
                //add(fileName);
                mergeCheckout(curCommit,filePath);//将当前分支的文件写入工作区,这么做是为了防止工作区有未追踪的更改
            } else if (curBlob.equals(sptBlob)) {//只有合并分支对文件做了改动
                if(newBlob.equals("null")){
                    rm(fileName);
                }else {
                    //mergeComBlobs.put(filePath,newBlob);
                    mergeCheckout(newCommit,filePath);//将合并分支的文件写入工作区
                    add(fileName);
                }
            } else if (newBlob.equals(sptBlob)) {//只有当前分支对文件做了改动
                mergeCheckout(curCommit,filePath);//直接把当前分支的文件写入工作区即可
            }
        }
        for(Map.Entry<String,String>entry:newCommit.blobToFile.entrySet()){
            String filePath=entry.getKey();
            String fileName=getFileName(filePath);
            String newBlob=entry.getValue();
            String sptBlob=spiltCommit.blobToFile.getOrDefault(filePath,"null");
            if(!curCommit.blobToFile.containsKey(filePath)){//包含的情况上面的循环已经讨论过了
                if(sptBlob.equals("null")){//祖先和当前分支都不存在该文件,那就用合并分支的
                    mergeCheckout(newCommit,filePath);
                    add(fileName);
                }else if(sptBlob.equals(newBlob)){//祖先和合并分支有相同文件版本,当前分支删除了,那就删除
                    //这里不用删除了,因为file本来就不在工作区
                }else {//祖先和合并分支有不同版本,且当前分支无,产生冲突
                    dealConflict(filePath,"null",newBlob);
                }
            }
        }
    }
    private static void mergeCheckout(Commit commit,String filePath){
        File file=new File(filePath);
        Blob blob=commit.getBlob(filePath);
        Utils.writeContents(file,blob.content);
    }//为merge定制的,用于将某个提交中的文件写入工作区
    private static void dealConflict(String filePath,String curFile,String newFile){
        String curStr;
        String newStr;
        if(curFile.equals("null")){
            curStr="empty file";
        }else {
            File curTar=Utils.join(BLOBS,curFile);
            Blob curBlob=Utils.readObject(curTar, Blob.class);
            curStr=new String(curBlob.content, StandardCharsets.UTF_8);
        }
        if(newFile.equals("null")){
            newStr="empty file";
        }else {
            File newTar=Utils.join(BLOBS,newFile);
            Blob newBlob=Utils.readObject(newTar, Blob.class);
            newStr=new String(newBlob.content, StandardCharsets.UTF_8);
        }
        File file=new File(filePath);
        String output="<<<<<<< HEAD\n"+curStr+"\n"+"=======\n"+newStr+"\n"+">>>>>>>";
        writeContents(file,output.getBytes(StandardCharsets.UTF_8));//////
        add(Utils.getFileName(filePath));//////////////
        System.out.println("Encountered a merge conflict.");
    }
    private static void mergeCommit(String message,String par1,String par2){
        Commit commit=new Commit(message,par1,par2);//创建commit对象,par1是curCom,par2是newCom
        String curBranch=getCurrentBranch();//获取当前分支名字
        File output=Utils.join(BRANCHES,curBranch);//获取当前分支路径
        Utils.writeObject(output,commit.ID);//让当前分支指向这个commit,其他分支指针不移动
    }
}
