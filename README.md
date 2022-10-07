# bat

Basic, awesome TAB plugin.

## Installation

#### Requirements

- Java 16+
- Velocity 3.0.0+

#### Steps

1. Download `bat-velocity.jar`.
2. Move `bat-velocity.jar` to your Velocity server's `plugins/` folder.
3. If there is no `plugins/bat/` folder, then start your Velocity server to generate the default configuration.
4. Configure `bat` to your heart's content.

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
| `<servername>`  | The local server's name that was registered with Velocity.                                                                           |                                                |
| `<playerping>`  | The player's ping to the proxy. Will just return the number without 'ms' or anything.                                                | `150`: int                                     |
| `<playeruuid>`  | The player's UUID, in the dash-separated format.                                                                                     | `b4dc78cf-6aeb-439f-8c68-4b5b2a4e340c`: String |
| `<playername>`  | The player's username.                                                                                                               | `Bluely_`: String                              |
| `<playerip>`    | The player's ip.                                                                                                                     | `127.0.0.1`: String                            |
| `<date>`        | The date. (yyyy/MM/dd)                                                                                                               | `2021/09/25`: String                           |
| `<time>`        | The time. (HH:mm a)                                                                                                                  | `5:24 PM`: String                              |
| `<datetime>`    | The datetime.                                                                                                                        | `2021/09/25 5:24 PM`: String                   |
