package com.wp.study.praxis.file;

import com.wp.study.base.util.CacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WinRarTools {

    private static final Logger LOG = LoggerFactory.getLogger(WinRarTools.class);

    public static final String WIN_RAR_PATH = "C:\\Program Files\\WinRAR\\WinRAR.exe";

    public static final String WIN_RAR_NAME = "WinRAR\\.exe";

    /**
     * 执行子目录内容的压缩
     *
     * @param winrar
     * @param dir
     * @param password
     * @param layer    压缩文件夹在当前目录层级数
     */
    public static void compress(File winrar, File dir, String password, int layer) {
        if (null == dir || !dir.exists() || !dir.isDirectory()) {
            LOG.error("can not find directory <{}>", dir);
        }

        List<File> parents = new ArrayList<File>();
        List<File> childs = null;
        parents.add(dir);
        for (int i = 0; i < layer; i++) {
            childs = new ArrayList<File>();
            if (null == parents || parents.isEmpty()) {
                continue;
            }
            for (File parent : parents) {
                if (!parent.isDirectory()) {
                    continue;
                }
                FileSystemView fsv = FileSystemView.getFileSystemView();
                // 系统顶级目录C:\，D:\...是隐藏属性
                if (fsv.isFileSystemRoot(parent) || !parent.isHidden()) {
                    File[] files = parent.listFiles();
                    if (files != null && files.length != 0) {
                        for (File file : files) {
                            childs.add(file);
                        }
                    }
                }
            }
            parents = childs;
        }

        if (childs != null && !childs.isEmpty()) {
            for (File comp : childs) {
                compress(winrar, comp, password);
            }
        }
    }

    /**
     * 压缩文件（夹）到同级目录，文件以rar标准格式压缩，文件夹以zip标准格式压缩 rar <命令> -<参数 1> -<参数 N> <压缩文件>
     * <文件...> <@列表文件...> <解压路径\> <命令> a：添加文件到压缩文件 <参数> hp[password]：加密文件数据和文件头
     * m<0..5>：设置压缩级别(0-存储...3-默认...5-最大)
     *
     * @param origin   需压缩文件
     * @param password 压缩密码，为空时无密码压缩
     * @return
     */
    public static boolean compress(File winrar, File origin, String password) {
        boolean result = false;
        if (origin.getName().matches("^[\\s\\S]*\\.(rar|zip|7z)$")) {
            LOG.error("<{}> has been compress file", origin);
            return result;
        }

        // 获取压缩文件路径（与源文件同级目录）
        String oriFileName = origin.getName();
        String compFileName = null;
        if (origin.isDirectory()) {
            compFileName = oriFileName + ".zip";
        } else {
            int separator = oriFileName.lastIndexOf(".");
            if (separator != -1) {
                compFileName = oriFileName.substring(0, separator) + ".rar";
            } else {
                compFileName = oriFileName + ".rar";
            }
        }
        File compFile = new File(origin.getParentFile(), compFileName);

        // 压缩文件已存在，退出程序
        if (compFile.exists()) {
            LOG.error("<{}> has existed", compFile);
            return result;
        }

        // 获取WinRAR压缩程序
        if (null == winrar || !winrar.exists()) {
            if (CacheUtils.exists("winrar")) {
                winrar = CacheUtils.getCache("winrar", File.class);
            } else {
                winrar = new File(WIN_RAR_PATH);
                if (winrar.exists()) {
                    CacheUtils.setCache("winrar", winrar);
                } else {
                    List<File> programs = search(WIN_RAR_NAME, true);
                    if (programs == null || programs.size() != 1) {
                        LOG.error("get compress program <{}> failed", WIN_RAR_NAME);
                        return result;
                    }
                    winrar = programs.get(0);
                    CacheUtils.setCache("winrar", programs.get(0));
                }
            }
        } else {
            // 缓存WinRAR
            CacheUtils.setCache("winrar", winrar);
        }

        // -m3采用标准方式压缩文件
        StringBuffer cmd = new StringBuffer(winrar.getPath());
        cmd.append(" a -m3");

        /*
         * 压缩时不保留父级目录结构 ep：完全排除目录路径 ep1：排除父级及以上目录路径 ep2：保留完整目录路径 ep3：保留包含盘符完整目录路径
         */
        cmd.append(" -ep1 ");

        // hp开关加密文件数据、文件名、大小、属性、注释等所有可感知压缩文件区域
        if (StringUtils.isNotBlank(password)) {
            cmd.append(" -hp").append(password);
        }

        try {
            // 添加压缩文件和源文件路径
            cmd.append(" ").append(compFile.getPath()).append(" ").append(origin.getPath());
            // 调用winrar程序进行文件压缩
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            if (proc.waitFor() == 0) { // 进程正常结束标志
                if (proc.exitValue() == 0) { // 子进程正常结束标志
                    LOG.info("succeed in executing command <{}>", cmd);
                    result = true;
                }
            }
        } catch (Exception e) {
            LOG.error("compress fail, origin={}, error:", origin, e);
        }
        return result;
    }

    /**
     * 压缩文件（夹）到同级目录，文件以rar标准格式压缩，文件夹以zip标准格式压缩 rar <命令> -<参数 1> -<参数 N> <压缩文件>
     * <文件...> <@列表文件...> <解压路径\> <命令> a：添加文件到压缩文件 <参数> hp[password]：加密文件数据和文件头
     * m<0..5>：设置压缩级别(0-存储...3-默认...5-最大)
     *
     * @param compressName   需压缩文件
     * @param compressModel
     * @param password 压缩密码，为空时无密码压缩
     * @return
     */
    public static boolean compress(String compressName, String compressModel, String password, File... origins) {
        boolean result = false;
        if (StringUtils.isBlank(compressName) || null == origins || origins.length == 0
                || StringUtils.isBlank(compressModel)) {
            LOG.error("invalid compress parameters");
            return result;
        }
        // 生成压缩文件真实名称
        String realCompressName = compressName + "." + compressModel;
        File originDirectory = origins[0].getParentFile();
        File compFile = new File(originDirectory, realCompressName);

        // 压缩文件已存在，退出程序
        if (compFile.exists()) {
            LOG.error("<{}> has existed", compFile);
            return result;
        }

        // 获取WinRAR压缩程序
        File winrar = new File(WIN_RAR_PATH);

        // -m3采用标准方式压缩文件
        StringBuffer cmd = new StringBuffer(winrar.getPath());
        cmd.append(" a -m3");

        /*
         * 压缩时不保留父级目录结构 ep：完全排除目录路径 ep1：排除父级及以上目录路径 ep2：保留完整目录路径 ep3：保留包含盘符完整目录路径
         */
        cmd.append(" -ep1 ");
        /**
         * -o+：覆盖同名压缩文件 -o-：不覆盖
         */
        cmd.append(" -o+ ");
        // 在后台运行
        cmd.append(" -ibck ");

        // hp开关加密文件数据、文件名、大小、属性、注释等所有可感知压缩文件区域
        if (StringUtils.isNotBlank(password)) {
            cmd.append(" -hp").append(password);
        }

        try {
            // 添加压缩文件和源文件路径
            cmd.append(" \"").append(compFile.getPath()).append("\"");
            for (File origin : origins) {
                if (!origin.getName().matches("^[\\s\\S]*\\.(rar|zip|7z)$")) {
                    cmd.append(" \"").append(origin.getPath()).append("\"");
                }
            }
            // 调用winrar程序进行文件压缩
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            if (proc.waitFor() == 0) { // 进程正常结束标志
                if (proc.exitValue() == 0) { // 子进程正常结束标志
                    LOG.info("succeed in executing command <{}>", cmd);
                    result = true;
                }
            }
        } catch (Exception e) {
            LOG.error("compress fail, compressName={}, compressModel={}, origins={}, error:", compressName,
                    compressModel, origins, e);
        }
        return result;
    }

    /**
     * 解压文件（夹）到同级目录 rar <命令> -<参数 1> -<参数 N> <压缩文件> <文件...> <@列表文件...> <解压路径\> <命令>
     * e：提取文件无需压缩文件的路径 l[t[a],b]：列出压缩文件内容 [technical[all], bare] x：以完整路径提取文件 <参数>
     * hp[password]：加密文件数据和文件头 m<0..5>：设置压缩级别(0-存储...3-默认...5-最大)
     *
     * @param origin   需解压文件
     * @param password 解压密码，为空时无密码解压
     * @return
     */
    public static boolean uncompress(File origin, String password) {
        boolean result = false;
        if (!origin.getName().matches("^[\\s\\S]*\\.(rar|zip|7z)$")) {
            LOG.error("<{}> is not compress file", origin);
            return result;
        }

        // 获取解压文件程序
        File winrar = null;
        if (CacheUtils.exists("winrar")) {
            winrar = CacheUtils.getCache("winrar", File.class);
        } else {
//			List<File> programs = search(WIN_RAR_NAME, true);
//			if (programs == null || programs.size() != 1) {
//				LOG.error("get compress program <{}> failed", WIN_RAR_PATH);
//				return result;
//			}
//			winrar = programs.get(0);
            winrar = new File(WIN_RAR_PATH);
//			CacheUtils.setCache("winrar", programs.get(0));
        }

        // x提取压缩文件中完整路径，-m3标准方式解压
        StringBuffer cmd = new StringBuffer(winrar.getPath());
        cmd.append(" x -m3");
        if (password != null) {
            // hp开关加密文件数据、文件名、大小、属性、注释等所有可感知压缩文件区域
            cmd.append(" -hp").append(password);
        }

        // 获取解压目录（与源文件同级目录）
        File uncompDir = origin.getParentFile();
        try {
            // 添加源文件路径和压缩目录
            cmd.append(" ").append(origin.getPath()).append(" ").append(uncompDir.getPath());
            // 调用winrar程序进行文件解压
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            if (proc.waitFor() == 0) { // 进程正常结束标志
                if (proc.exitValue() == 0) { // 子进程正常结束标志
                    LOG.info("succeed in executing command <{}>", cmd);
                    result = true;
                }
            }
        } catch (Exception e) {
            LOG.error("uncompress fail, origin={}, error:", origin, e);
        }
        return result;
    }

    /**
     * 获取系统中匹配表达式的所有文件
     *
     * @param regex     正则表达式
     * @param ignoreDir 是否忽略文件夹，true：忽略，false：保留
     * @return
     */
    private static List<File> search(String regex, boolean ignoreDir) {
        List<File> matches = new ArrayList<File>();
        // 获取系统盘符
        File[] roots = File.listRoots();
        if (null == roots || roots.length == 0) {
            return matches;
        }

        FileSystemView fsv = FileSystemView.getFileSystemView();
        for (File root : roots) {
            if (!fsv.isFileSystemRoot(root) || root.isFile()) {
                continue;
            }

            List<File> tempDirs = new ArrayList<File>();
            tempDirs.add(root);
            while (tempDirs.size() > 0) {
                // 检验当前文件夹是否匹配
                File curDir = tempDirs.get(0);
                tempDirs.remove(0);
                if (curDir.getName().matches(regex) && !ignoreDir) {
                    matches.add(curDir);
                }
                // 检验
                File[] files = curDir.listFiles();
                if (null == files || files.length == 0) {
                    continue;
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        tempDirs.add(file);
                        continue;
                    }
                    if (file.getName().matches(regex)) {
                        matches.add(file);
                    }
                }
            }
        }
        return matches;
    }

}
