# Whale Watcher

Watches for container updates by looking for updated [semver](https://semver.org/) tags.
When updates are found a messages is sent to telegram.

You can ignore containers by adding a `whale-watcher.ignore=true` label to the container.

Containers with no tag, or a non-semver tag are ignored (currently).


## Running

```bash
$ docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -e WATCH_SCHEDULE=<valid crontab> \
  -e TELEGRAM_TOKEN=<your token> \
  -e TELEGRAM_CHAT_ID=<your chat id> \
  ghcr.io/akeboshiwind/whale-watcher:latest
```

Or with docker-compose:

```yaml
version: "3.8"

services:
  whale-watcher:
    image: ghcr.io/akeboshiwind/whale-watcher:latest
    restart: unless-stopped
    environment:
      - TZ=Europe/London
      - WATCH_SCHEDULE=<valid crontab>
      - TELEGRAM_TOKEN=<your token>
      - TELEGRAM_CHAT_ID=<your chat id>
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
```
