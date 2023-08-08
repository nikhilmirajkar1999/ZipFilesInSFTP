package com.example;
import com.jcraft.jsch.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
public class Main {
    public String zipFiles(String hostname,int port,String username, String password,String sourceDir,String OutputDir,String ZipFilename)
	{
		JSch jsch = new JSch();
        Session session = null;
        String status;
        try {
            session = jsch.getSession(username, hostname, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Get list of files in the remote folder
            Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(sourceDir);

            // Create an in-memory output stream for zip data
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);

            for (ChannelSftp.LsEntry entry : fileList) {
                if (!entry.getAttrs().isDir()) {
                    String fileName = entry.getFilename();
                    ZipArchiveEntry zipEntry = new ZipArchiveEntry(fileName);
                    zipOutputStream.putArchiveEntry(zipEntry);

                    // Read file content from SFTP and write to zip output stream
                    InputStream inputStream = channelSftp.get(sourceDir + "/" + fileName);
                    IOUtils.copy(inputStream, zipOutputStream);
                    inputStream.close();

                    zipOutputStream.closeArchiveEntry();
                }
            }

            zipOutputStream.close();

            // Upload the compressed data back to SFTP server
            ByteArrayInputStream compressedInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            channelSftp.put(compressedInputStream, OutputDir +"/"+ ZipFilename);
            compressedInputStream.close();

            // Disconnect SFTP session
            channelSftp.disconnect();
            session.disconnect();
            status="Zipped files successfully!";
        } catch (JSchException | SftpException | IOException e) {
            //e.printStackTrace();
            //System.out.println("Failed");
             status="Failed";
        }
        return status;
	}
    public static void main(String[] args){
      Main m = new Main();
      String status=m.zipFiles("147.154.151.252",5014,"nikhil.mirajkaryuvaraja@version1.com","Oracle@917546","/home/users/nikhil.mirajkaryuvaraja@version1.com/practice","/home/users/nikhil.mirajkaryuvaraja@version1.com/practice/sample","Archive.zip");
      System.out.println(status);
    }
}