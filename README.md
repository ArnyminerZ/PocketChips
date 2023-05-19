# PocketChips
![Documentation](https://img.shields.io/github/actions/workflow/status/ArnyminerZ/PocketChips/build-kdoc.yml?label=Documentation&logo=kotlin&style=for-the-badge)

Experience the thrill of poker without physical tokens. Play and bid wirelessly with Pocket Poker
Connect, an offline Android app that connects devices via Bluetooth. No chips needed, just pure
poker excitement in the palm of your hand. 

## Required Permissions
The app uses the [Nearby Connections API](https://developers.google.com/nearby/connections) from Google Play Services, which requires the following
permissions according to [the documentation](https://developers.google.com/nearby/connections/android/get-started#request_permissions):
`ACCESS_FINE_LOCATION`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`. 

## Pipeline
The button is rotated every round. Who has the button deals the cards. The player on his/her left
pays the small blind (`Game.smallBlind`), and the player to the left of this last one pays the large
blind (`Game.largeBlind`). The next player starts betting.

Once it's your turn, you have three options:
1. Call: match the amount of the big blind/biggest bet.
2. Raise: increase the bet within the specific limits of the game.
3. Fold: throw the hand away (leaves the round).
