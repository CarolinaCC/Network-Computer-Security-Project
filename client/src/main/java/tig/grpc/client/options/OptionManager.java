package tig.grpc.client.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import tig.grpc.client.Client;
import tig.grpc.client.operations.CustomProtocolOperations;
import tig.grpc.client.operations.Operations;
import tig.grpc.contract.Tig;


public class OptionManager {

    public static Options createOptions() {
        Options options = new Options();

        Option register = new Option("n", "Use to register new user");
        register.setArgs(0);
        register.setRequired(false);
        options.addOption(register);

        Option download = new Option("d", "Use to download a file");
        download.setArgs(3);
        download.setArgName("filename owner filepath");
        register.setRequired(false);
        options.addOption(download);

        Option delete = new Option("r", "Use to remove (delete) a file");
        delete.setArgs(1);
        delete.setArgName("filename");
        register.setRequired(false);
        options.addOption(delete);

        Option list = new Option("l", "Use to list all files");
        list.setArgs(0);
        register.setRequired(false);
        options.addOption(list);

        Option access = new Option("c", "Use to change Access Control options of a file");
        access.setArgs(3);
        access.setArgName("filename permission target");
        register.setRequired(false);
        options.addOption(access);

        Option upload = new Option("u", "Use to upload a new file");
        upload.setArgs(2);
        upload.setArgName("filename filepath");
        register.setRequired(false);
        options.addOption(upload);

        Option edit = new Option("e", "Use to edit a file");
        edit.setArgs(3);
        edit.setArgName("filename owner filepath");
        register.setRequired(false);
        options.addOption(edit);

        Option recover = new Option("b", "Use to list backup files");
        edit.setArgs(0);
        register.setRequired(false);
        options.addOption(recover);

        return options;
    }

    public static void executeOptions(CommandLine cmd, Client client) {
        if (cmd.hasOption('n')) {
            Operations.registerClient(client);
            return;
        }
        Operations.loginClient(client);

        if (cmd.hasOption('d')) {
            Operations.downloadFile(client, cmd.getOptionValues('d')[0], cmd.getOptionValues('d')[1], cmd.getOptionValues('d')[2]);
            return;
        }
        if (cmd.hasOption('u')) {
            Operations.uploadFile(client, cmd.getOptionValues('u')[0], cmd.getOptionValues('u')[1]);
            return;
        }
        if (cmd.hasOption('e')) {
            Operations.editFile(client, cmd.getOptionValues('e')[0], cmd.getOptionValues('e')[1], cmd.getOptionValues('e')[2]);
            return;
        }
        if (cmd.hasOption('r')) {
            Operations.deleteFile(client, cmd.getOptionValues('r')[0]);
            return;
        }
        if (cmd.hasOption('l')) {
            Operations.listFiles(client);
            return;
        }
        if (cmd.hasOption('b')) {
            Operations.listRecoverFiles(client);
            return;
        }
        if (cmd.hasOption('c')) {
            switch (cmd.getOptionValues("c")[1]) {
                case "READ":
                    Operations.setAccessControl(client, cmd.getOptionValues('c')[0], cmd.getOptionValues('c')[2], Tig.PermissionEnum.READ);
                    break;
                case "WRITE":
                    Operations.setAccessControl(client, cmd.getOptionValues('c')[0], cmd.getOptionValues('c')[2], Tig.PermissionEnum.WRITE);
                    break;
                case "NONE":
                    Operations.setAccessControl(client, cmd.getOptionValues('c')[0], cmd.getOptionValues('c')[2], Tig.PermissionEnum.NONE);
                    break;
                default:
                    System.out.println("Invalid permission value, can only be READ, WRITE or NONE");
            }
        }
    }
}
