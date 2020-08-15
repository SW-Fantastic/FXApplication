module fx.framework.resource {

   requires fx.framework.core;
   requires javafx.graphics;
   requires slf4j.api;

   exports org.swdc.fx.resource;
   exports org.swdc.fx.resource.icons;
   exports org.swdc.fx.resource.source;

   provides org.swdc.fx.extra.IconSPIService with org.swdc.fx.resource.icons.FontawsomeService;
   provides org.swdc.fx.extra.ExtraLoader with org.swdc.fx.resource.ResourceLoader;
}