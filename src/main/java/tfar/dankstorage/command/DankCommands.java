package tfar.dankstorage.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import tfar.dankstorage.DankStorage;

public class DankCommands {

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal(DankStorage.MODID)
                .then(Commands.literal("clear")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                        .then(Commands.literal("all")
                                .executes(DankCommands::clearAll))

                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(DankCommands::clearID))
                )

                .then(Commands.literal("set_tier")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .then(Commands.argument("tier", IntegerArgumentType.integer(1,7))
                                        .executes(DankCommands::setTier)
                                )
                        )
                )


                .then(Commands.literal("lock")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(DankCommands::lock)
                        )
                )

                .then(Commands.literal("unlock")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                        .then(Commands.argument("id", IntegerArgumentType.integer(0))
                                .executes(DankCommands::unlock)
                        )
                )
        );
    }

    private static int clearAll(CommandContext<CommandSourceStack> context) {
        DankStorage.instance.data.clearAll();
        return 1;
    }

    private static int clearID(CommandContext<CommandSourceStack> context) {
        int id = IntegerArgumentType.getInteger(context, "id");
        boolean success = DankStorage.instance.data.clearId(id);
        if (!success) {
            throw new CommandRuntimeException(Component.translatable("dankstorage.command.clear_id.invalid_id"));
        }
        return 1;
    }

    private static int setTier(CommandContext<CommandSourceStack> context) {
        int id = IntegerArgumentType.getInteger(context, "id");
        int tier = IntegerArgumentType.getInteger(context, "tier");
        boolean success = DankStorage.instance.data.setTier(id, tier);
        if (!success) {
            throw new CommandRuntimeException(Component.translatable("dankstorage.command.set_tier.invalid_id"));
        }
        return 1;
    }

    private static int lock(CommandContext<CommandSourceStack> context) {
        int id = IntegerArgumentType.getInteger(context, "id");
        boolean success = DankStorage.instance.data.lock(id);
        if (!success) {
            throw new CommandRuntimeException(Component.translatable("dankstorage.command.lock.invalid_id"));
        }
        return 1;
    }

    private static int unlock(CommandContext<CommandSourceStack> context) {
        int id = IntegerArgumentType.getInteger(context, "id");
        boolean success = DankStorage.instance.data.unlock(id);
        if (!success) {
            throw new CommandRuntimeException(Component.translatable("dankstorage.command.unlock.invalid_id"));
        }
        return 1;
    }
}
