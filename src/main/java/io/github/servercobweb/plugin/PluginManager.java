package io.github.servercobweb.plugin;

import io.github.servercobweb.Server;
import io.github.servercobweb.command.PluginCommand;
import io.github.servercobweb.command.SimpleCommandMap;
import io.github.servercobweb.event.*;
import io.github.servercobweb.utils.MainLogger;
import io.github.servercobweb.utils.PluginException;
import io.github.servercobweb.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class PluginManager {

    private Server server;

    private SimpleCommandMap commandMap;

    protected Map<String, Plugin> plugins = new LinkedHashMap<>();

    protected Map<String, PluginLoader> fileAssociations = new HashMap<>();

    public static boolean useTimings = false;

    public PluginManager(Server server, SimpleCommandMap commandMap) {
        this.server = server;
        this.commandMap = commandMap;
    }

    public Plugin getPlugin(String name) {
        if (this.plugins.containsKey(name)) {
            return this.plugins.get(name);
        }
        return null;
    }

    public boolean registerInterface(Class<? extends PluginLoader> loaderClass) {
        if (loaderClass != null) {
            try {
                Constructor constructor = loaderClass.getDeclaredConstructor(Server.class);
                constructor.setAccessible(true);
                this.fileAssociations.put(loaderClass.getName(), (PluginLoader) constructor.newInstance(this.server));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public Map<String, Plugin> getPlugins() {
        return plugins;
    }

    public Plugin loadPlugin(String path) {
        return this.loadPlugin(path, null);
    }

    public Plugin loadPlugin(File file) {
        return this.loadPlugin(file, null);
    }

    public Plugin loadPlugin(String path, Map<String, PluginLoader> loaders) {
        return this.loadPlugin(new File(path), loaders);
    }

    public Plugin loadPlugin(File file, Map<String, PluginLoader> loaders) {
        for (PluginLoader loader : (loaders == null ? this.fileAssociations : loaders).values()) {
            for (Pattern pattern : loader.getPluginFilters()) {
                if (pattern.matcher(file.getName()).matches()) {
                    PluginDescription description = loader.getPluginDescription(file);
                    if (description != null) {
                        try {
                            Plugin plugin = loader.loadPlugin(file);
                            if (plugin != null) {
                                this.plugins.put(plugin.getDescription().getName(), plugin);

                                List<PluginCommand> pluginCommands = this.parseYamlCommands(plugin);

                                if (!pluginCommands.isEmpty()) {
                                    this.commandMap.registerAll(plugin.getDescription().getName(), pluginCommands);
                                }

                                return plugin;
                            }
                        } catch (Exception e) {
                            return null;
                        }
                    }
                }
            }
        }

        return null;
    }

    public Map<String, Plugin> loadPlugins(String dictionary) {
        return this.loadPlugins(new File(dictionary));
    }

    public Map<String, Plugin> loadPlugins(File dictionary) {
        return this.loadPlugins(dictionary, null);
    }

    public Map<String, Plugin> loadPlugins(String dictionary, List<String> newLoaders) {
        return this.loadPlugins(new File(dictionary), newLoaders);
    }

    public Map<String, Plugin> loadPlugins(File dictionary, List<String> newLoaders) {
        return this.loadPlugins(dictionary, newLoaders, false);
    }

    public Map<String, Plugin> loadPlugins(File dictionary, List<String> newLoaders, boolean includeDir) {
        if (dictionary.isDirectory()) {
            Map<String, File> plugins = new LinkedHashMap<>();
            Map<String, Plugin> loadedPlugins = new LinkedHashMap<>();
            Map<String, List<String>> dependencies = new LinkedHashMap<>();
            Map<String, List<String>> softDependencies = new LinkedHashMap<>();
            Map<String, PluginLoader> loaders = new LinkedHashMap<>();
            if (newLoaders != null) {
                for (String key : newLoaders) {
                    if (this.fileAssociations.containsKey(key)) {
                        loaders.put(key, this.fileAssociations.get(key));
                    }
                }
            } else {
                loaders = this.fileAssociations;
            }

            for (final PluginLoader loader : loaders.values()) {
                for (File file : dictionary.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        for (Pattern pattern : loader.getPluginFilters()) {
                            if (pattern.matcher(name).matches()) {
                                return true;
                            }
                        }
                        return false;
                    }
                })) {
                    if (file.isDirectory() && !includeDir) {
                        continue;
                    }
                    try {
                        PluginDescription description = loader.getPluginDescription(file);
                        if (description != null) {
                            String name = description.getName();
                            if (name.toLowerCase().contains("nukkit") || name.toLowerCase().contains("minecraft") || name.toLowerCase().contains("mojang")) {
                                this.server.getLogger().error(this.server.getLanguage().translateString("nemisys.plugin.loadError", new String[]{name, "%nemisys.plugin.restrictedName"}));
                                continue;
                            } else if (name.contains(" ")) {
                                this.server.getLogger().warning(this.server.getLanguage().translateString("nemisys.plugin.spacesDiscouraged", name));
                            }

                            if (plugins.containsKey(name) || this.getPlugin(name) != null) {
                                this.server.getLogger().error(this.server.getLanguage().translateString("nemisys.plugin.duplicateError", name));
                                continue;
                            }

                            boolean compatible = false;

                            for (String version : description.getCompatibleAPIs()) {

                                //Check the format: majorVersion.minorVersion.patch
                                if (!Pattern.matches("[0-9]\\.[0-9]\\.[0-9]", version)) {
                                    this.server.getLogger().error(this.server.getLanguage().translateString("nemisys.plugin.loadError", new String[]{name, "Wrong API format"}));
                                    continue;
                                }

                                String[] versionArray = version.split("\\.");
                                String[] apiVersion = this.server.getApiVersion().split("\\.");

                                //Completely different API version
                                if (!Objects.equals(Integer.valueOf(versionArray[0]), Integer.valueOf(apiVersion[0]))) {
                                    continue;
                                }

                                //If the plugin requires new API features, being backwards compatible
                                if (Integer.valueOf(versionArray[1]) > Integer.valueOf(apiVersion[1])) {
                                    continue;
                                }

                                compatible = true;
                                break;
                            }

                            if (!compatible) {
                                this.server.getLogger().error(this.server.getLanguage().translateString("nemisys.plugin.loadError", new String[]{name, "%nemisys.plugin.incompatibleAPI"}));
                            }

                            plugins.put(name, file);

                            softDependencies.put(name, description.getSoftDepend());

                            dependencies.put(name, description.getDepend());

                            for (String before : description.getLoadBefore()) {
                                if (softDependencies.containsKey(before)) {
                                    softDependencies.get(before).add(name);
                                } else {
                                    List<String> list = new ArrayList<>();
                                    list.add(name);
                                    softDependencies.put(before, list);
                                }
                            }
                        }
                    } catch (Exception e) {
                        this.server.getLogger().error(this.server.getLanguage().translateString("nemisys.plugin" +
                                ".fileError", new String[]{file.getName(), dictionary.toString(), Utils
                                .getExceptionMessage(e)}));
                        MainLogger logger = this.server.getLogger();
                        if (logger != null) {
                            logger.logException(e);
                        }
                    }
                }
            }

            while (!plugins.isEmpty()) {
                boolean missingDependency = true;
                for (String name : new ArrayList<>(plugins.keySet())) {
                    File file = plugins.get(name);
                    if (dependencies.containsKey(name)) {
                        for (String dependency : new ArrayList<>(dependencies.get(name))) {
                            if (loadedPlugins.containsKey(dependency) || this.getPlugin(dependency) != null) {
                                dependencies.get(name).remove(dependency);
                            } else if (!plugins.containsKey(dependency)) {
                                this.server.getLogger().critical(this.server.getLanguage().translateString("nukkit" +
                                        ".plugin.loadError", new String[]{name, "%nemisys.plugin.unknownDependency"}));
                                break;
                            }
                        }

                        if (dependencies.get(name).isEmpty()) {
                            dependencies.remove(name);
                        }
                    }

                    if (softDependencies.containsKey(name)) {
                        for (String dependency : new ArrayList<>(softDependencies.get(name))) {
                            if (loadedPlugins.containsKey(dependency) || this.getPlugin(dependency) != null) {
                                softDependencies.get(name).remove(dependency);
                            }
                        }

                        if (softDependencies.get(name).isEmpty()) {
                            softDependencies.remove(name);
                        }
                    }

                    if (!dependencies.containsKey(name) && !softDependencies.containsKey(name)) {
                        plugins.remove(name);
                        missingDependency = false;
                        Plugin plugin = this.loadPlugin(file, loaders);
                        if (plugin != null) {
                            loadedPlugins.put(name, plugin);
                        } else {
                            this.server.getLogger().critical(this.server.getLanguage().translateString("nemisys.plugin.genericLoadError", name));
                        }
                    }
                }

                if (missingDependency) {
                    for (String name : new ArrayList<>(plugins.keySet())) {
                        File file = plugins.get(name);
                        if (!dependencies.containsKey(name)) {
                            softDependencies.remove(name);
                            plugins.remove(name);
                            missingDependency = false;
                            Plugin plugin = this.loadPlugin(file, loaders);
                            if (plugin != null) {
                                loadedPlugins.put(name, plugin);
                            } else {
                                this.server.getLogger().critical(this.server.getLanguage().translateString("nemisys.plugin.genericLoadError", name));
                            }
                        }
                    }

                    if (missingDependency) {
                        for (String name : plugins.keySet()) {
                            this.server.getLogger().critical(this.server.getLanguage().translateString("nemisys.plugin.loadError", new String[]{name, "%nemisys.plugin.circularDependency"}));
                        }
                        plugins.clear();
                    }
                }
            }

            return loadedPlugins;
        } else {

            return new HashMap<>();
        }
    }

    public boolean isPluginEnabled(Plugin plugin) {
        if (plugin != null && this.plugins.containsKey(plugin.getDescription().getName())) {
            return plugin.isEnabled();
        } else {
            return false;
        }
    }

    public void enablePlugin(Plugin plugin) {
        if (!plugin.isEnabled()) {
            try {
                plugin.getPluginLoader().enablePlugin(plugin);
            } catch (Exception e) {
                MainLogger logger = this.server.getLogger();
                if (logger != null) {
                    logger.logException(e);
                }
                this.disablePlugin(plugin);
            }
        }
    }

    protected List<PluginCommand> parseYamlCommands(Plugin plugin) {
        List<PluginCommand> pluginCmds = new ArrayList<>();

        for (Map.Entry entry : plugin.getDescription().getCommands().entrySet()) {
            String key = (String) entry.getKey();
            Object data = entry.getValue();
            if (key.contains(":")) {
                this.server.getLogger().critical(this.server.getLanguage().translateString("nemisys.plugin.commandError", new String[]{key, plugin.getDescription().getFullName()}));
                continue;
            }
            if (data instanceof Map) {
                PluginCommand newCmd = new PluginCommand<>(key, plugin);

                if (((Map) data).containsKey("description")) {
                    newCmd.setDescription((String) ((Map) data).get("description"));
                }

                if (((Map) data).containsKey("usage")) {
                    newCmd.setUsage((String) ((Map) data).get("usage"));
                }

                if (((Map) data).containsKey("aliases")) {
                    Object aliases = ((Map) data).get("aliases");
                    if (aliases instanceof List) {
                        List<String> aliasList = new ArrayList<>();
                        for (String alias : (List<String>) aliases) {
                            if (alias.contains(":")) {
                                this.server.getLogger().critical(this.server.getLanguage().translateString("nemisys.plugin.aliasError", new String[]{alias, plugin.getDescription().getFullName()}));
                                continue;
                            }
                            aliasList.add(alias);
                        }

                        newCmd.setAliases(aliasList.stream().toArray(String[]::new));
                    }
                }

                pluginCmds.add(newCmd);
            }
        }

        return pluginCmds;
    }

    public void disablePlugins() {
        for (Plugin plugin : this.getPlugins().values()) {
            this.disablePlugin(plugin);
        }
    }

    public void disablePlugin(Plugin plugin) {
        if (plugin.isEnabled()) {
            try {
                plugin.getPluginLoader().disablePlugin(plugin);
            } catch (Exception e) {
                MainLogger logger = this.server.getLogger();
                if (logger != null) {
                    logger.logException(e);
                }
            }

            this.server.getScheduler().cancelTask(plugin);
            HandlerList.unregisterAll(plugin);
        }
    }

    public void clearPlugins() {
        this.disablePlugins();
        this.plugins.clear();
        this.fileAssociations.clear();
    }

    public void callEvent(Event event) {
        try {
            for (RegisteredListener registration : getEventListeners(event.getClass()).getRegisteredListeners()) {
                if (!registration.getPlugin().isEnabled()) {
                    continue;
                }

                try {
                    registration.callEvent(event);
                } catch (Exception e) {
                    this.server.getLogger().critical(this.server.getLanguage().translateString("nemisys.plugin.eventError", new String[]{event.getEventName(), registration.getPlugin().getDescription().getFullName(), e.getMessage(), registration.getListener().getClass().getName()}));
                    MainLogger logger = this.server.getLogger();
                    if (logger != null) {
                        logger.logException(e);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            Server.getInstance().getLogger().logException(e);
        }
    }

    public void registerEvents(Listener listener, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new PluginException("Plugin attempted to register " + listener.getClass().getName() + " while not enabled");
        }

        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
        Set<Method> methods;
        try {
            Method[] publicMethods = listener.getClass().getMethods();
            Method[] privateMethods = listener.getClass().getDeclaredMethods();
            methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
            Collections.addAll(methods, publicMethods);
            Collections.addAll(methods, privateMethods);
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().error("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return;
        }

        for (final Method method : methods) {
            final EventHandler eh = method.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            final Class<?> checkClass;

            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getLogger().error(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }

            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.get(eventClass);
            if (eventSet == null) {
                eventSet = new HashSet<>();
                ret.put(eventClass, eventSet);
            }

            for (Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
                // This loop checks for extending deprecated events
                if (clazz.getAnnotation(Deprecated.class) != null) {
                    if (Boolean.valueOf(String.valueOf(this.server.getConfig("settings.deprecated-verbpse", true)))) {
                        this.server.getLogger().warning(this.server.getLanguage().translateString("nemisys.plugin.deprecatedEvent", new String[]{plugin.getName(), clazz.getName(), listener.getClass().getName() + "." + method.getName() + "()"}));
                    }
                    break;
                }
            }
            this.registerEvent(eventClass, listener, eh.priority(), new MethodEventExecutor(method), plugin, eh.ignoreCancelled());
        }
    }

    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) throws PluginException {
        this.registerEvent(event, listener, priority, executor, plugin, false);
    }

    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled) throws PluginException {
        if (!plugin.isEnabled()) {
            throw new PluginException("Plugin attempted to register " + event + " while not enabled");
        }
        try {
            this.getEventListeners(event).register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
        } catch (IllegalAccessException e) {
            Server.getInstance().getLogger().logException(e);
        }
    }

    private HandlerList getEventListeners(Class<? extends Event> type) throws IllegalAccessException {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlers");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalAccessException(Utils.getExceptionMessage(e));
        }
    }

    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) throws IllegalAccessException {
        try {
            clazz.getDeclaredMethod("getHandlers");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlers method required!");
            }
        }
    }

    public boolean useTimings(){
        return useTimings;
    }

    public void setUseTimings(boolean use){
        useTimings = use;
    }

}
