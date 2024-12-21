# vscode-jdkmanager
A proof-of-concept-extension to manage JDKs, built in Scala.

<img width="865" alt="image" src="https://github.com/user-attachments/assets/063aaf90-050c-4002-9082-e122d039bd01" />

## Implemented commands
- Discover JDKs — autodiscovers JDKs
- Report env.JAVA_HOME — Shows the output of `process.env.get("JAVA_HOME")`
- Select JDK — let's the user select a JDK major version from a list of installed ones, for use in the workspace.


## TODO
- [x] JDK detection
- [x] User-wide JDK configuration
- [x] Workspace JDK selection
- [ ] Tests!
- [ ] Resolving errors due to project JDKs not being installed or configured in the user settings
- [ ] Folder/Project JDK selection
- [ ] UI-improvements

## Building/running
- Compile the extension with `mill extension.fastLinkJS`
- The preconfigured launch configuration should start the extension in the extension host. Everything after that should
be pretty self explanatory.
