# bat

Basic, awesome TAB plugin.

## Installation

#### Requirements

- Java 17+
- The latest **development** version of Velocity *(This plugin does not work on the Stable version!)*
- Git (This is only required if you intend on cloning the repository through your Terminal)

#### Compilation

- Clone this repository to a directory of your choice through running `git clone https://github.com/NearVanilla/bat.git` in your terminal. Alternatively, you can download this repository as a ZIP at the top of this page.
- Change your directory to the `bat` folder through running `cd bat`
- Run `gradlew build` to compile the project *(Ensure you have the correct version of Java)*.
- Navigate to the `libs` folder inside of the newly created `build` folder in your File Explorer.
- Drag the `bat-velocity.jar` file into the `plugins` folder of your Velocity Server.
- If you do not have a `plugins/bat` folder, then start your Velocity server to generate the default configuration.
- Configure `bat` to your liking!


## Placeholders

By default, bat provides a set of placeholders using [MiniMessage's template system](https://docs.adventure.kyori.net/minimessage#template).

`<date>`, `<time>`, and `<datetime>` are parsed using Java's [SimpleDateFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/SimpleDateFormat.html), meaning that the players will be seeing the server's local time, not their own local time.

| Template Tag    | Description                                                                                                                          | Example Value                                  |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------|
| `<proxycount>`  | The amount of players on the proxy.                                                                                                  | `10`: int                                      |
| `<proxymax>`    | The maximum amount of players allowed on the proxy. This is the same as the value of 'show-max-players' in Velocity's configuration. | `100`: int                                     |
| `<proxymotd>`   | The proxy's MOTD component.                                                                                                          | `A Proxy Server`: Component                    |
| `<servercount>` | The amount of players on the local server.                                                                                           | `10`: int                                      |
| `<servermax>`   | The maximum amount of players allowed on the local server.                                                                           | `100`: int                                     |
| `<servermotd>`  | The local server's MOTD component.                                                                                                   | `A Minecraft Server`: Component                |
| `<playerserver>`  | The local server's name that was registered with Velocity.                                                                           |                                                |
| `<playerping>`  | The player's ping to the proxy. Will just return the number without 'ms' or anything.                                                | `150`: int                                     |
| `<playeruuid>`  | The player's UUID, in the dash-separated format.                                                                                     | `b4dc78cf-6aeb-439f-8c68-4b5b2a4e340c`: String |
| `<playername>`  | The player's username.                                                                                                               | `Bluely_`: String                              |
| `<playerip>`    | The player's ip.                                                                                                                     | `127.0.0.1`: String                            |
| `<date>`        | The date. (yyyy/MM/dd)                                                                                                               | `2021/09/25`: String                           |
| `<time>`        | The time. (HH:mm a)                                                                                                                  | `5:24 PM`: String                              |
| `<datetime>`    | The datetime.                                                                                                                        | `2021/09/25 5:24 PM`: String                   |
