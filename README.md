Page Editor
======================

This plugin provide as way to edit pages, view the files on the server, and create new pages. Additionally,
the entire project is editable via webdav.

The easiest way to use this is to include it as an ivy dependency like:

> <dependency org="com.emergentideas" name="page-editor" rev="latest.integration" conf="appdep" />

And then add the following lines to the configuration.

> # add the page editing conf
> class-path-include->com/emergentideas/page/editor/configuration/page-editor.conf
> 
> # add the app's handlers
> com.emergentideas.page.editor.handles.PublicHandle
> com.emergentideas.page.editor.handles.MenuHandle

The PublicHandle and MenuHandle are not necessary. The menu handle will show the menu items for viewing pages and
the PublicHandle will redirect the default top level directory request to index.html.

These components assuming that there is an authentication service present.  If that is not the case, they
can not be used.

To mount the web dav share on Linux use a command like:

> mount.davfs http://localhost:8080/webdav/ /tmp/davtest

The terminating slash on the URL is really important. It must be there. The command will ask for 
a user and password. That user must be part of the administrators group according to the authentication
interface configured for the app.

The above webdav configuration will only share the static_content directory. If you want to share everything,
and this is a little dangerous, instead of including the above configuration include the configuration like:

> # add the page editing conf
> class-path-include->com/emergentideas/page/editor/configuration/page-editor-webdav-all.conf

