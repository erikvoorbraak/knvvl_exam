package org.knvvl.exam.backup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

class DropboxTest
{
    @Disabled
    @Test
    void helloWorld() throws DbxException, IOException
    {
        String accessToken = "<your access token>";
        DbxRequestConfig config = DbxRequestConfig.newBuilder("knvvl/exam-upload").build();
        DbxClientV2 client = new DbxClientV2(config, accessToken);

        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        String filename = "Export2023-10-05_202758.json";
        Path file = Path.of("C:\\Users\\vooeri0.SAI360\\Downloads\\", filename);
        try (InputStream in = new FileInputStream(file.toFile())) {
            client.files().uploadBuilder("/" + filename).uploadAndFinish(in);
        }
    }
}
