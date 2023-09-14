## About the project

Android application that provides the network level signal for 2G (GSM), WCDMA (3G), and, LTE (4G) in your current location. Additionally, the application can display the specific tower to which your mobile is connected.

### Built With
![Java]

## Getting Started

### Prerequisites
* Java 1.8 or superior
* Android Studio
* Google Maps API Key.


### Installation
1. Clone the repo.
```sh
https://github.com/JuananMtez/Umu-Network-Cell-Info.git
```

2. Open the project with Android Studio

4. Install dependencies. 


## Usage

Run the application with Android Studio.


## Aditional information

You must change the API KEY in ``./app/src/release/res/values/google_maps_api.xml``.
```xml
<resources>
    <!--
    TODO: Before you release your application, you need a Google Maps API key.

    To do this, you can either add your release key credentials to your existing
    key, or create a new key.

    Note that this file specifies the API key for the release build target.
    If you have previously set up a key for the debug target with the debug signing certificate,
    you will also need to set up a key for your release certificate.

    Follow the directions here:

    https://developers.google.com/maps/documentation/android/signup

    Once you have your key (it starts with "AIza"), replace the "google_maps_key"
    string in this file.
    -->
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_KEY_HERE</string>
</resources>

```




## Author

* **Juan Antonio Martínez López** - [Website](https://juananmtez.github.io/) - [LinkedIn](https://www.linkedin.com/in/juanantonio-martinez/)


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

[Java]: https://img.shields.io/badge/java-20232A?style=for-the-badge&logo=openjdk
