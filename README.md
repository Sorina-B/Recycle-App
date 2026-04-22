# Android Project

## Project Title
Recycle App
## Description
This app is a daily guide to learn how to recycle properly. By scanning the barcode of any product packaging, using the scanner functionality 
of the app, instantly receive correct instructions about the packaging composition, its enviromental impact, and most importantly, precise
instruction on how to dispose it correctly, in such way that the impact above our planet is minimum. Saving the scanning history is also 
possible, all recent searching history being saved in the cloud, allowing to keep track of eco-friendly habits.

## Features
- Built in Barcode scanner: scan product barcodes using the device's camera;
- Recycling Data: real-time packaging material and dsiposal instructions via the Open Food Facts database;
- Cloud scanning history: save recent searches to the cloud and access scanning history;

## Screenshots
![WhatsApp Image 2026-04-11 at 17 17 40 (1)](https://github.com/user-attachments/assets/9945cc33-1f94-4837-a1a3-b29f881f0694)
![WhatsApp Image 2026-04-11 at 17 17 40](https://github.com/user-attachments/assets/483e341d-88cf-4ac7-99c5-8caa21d31d02)
![WhatsApp Image 2026-04-11 at 17 17 40 (3)](https://github.com/user-attachments/assets/bc077a78-0dd0-482d-9957-66149f447a58)
![WhatsApp Image 2026-04-11 at 17 17 40 (2)](https://github.com/user-attachments/assets/e5eaf437-4087-4eff-bdd9-304be7c6f0c3)

## Technologies Used
- Kotlin: core programming lanquage
- CameraX: for handling camera lifecycle and preview
- Google ML Kit: for fast, on-device barcode and QR code scanning
- Firebase Realtime Database: for storing and retrieving user scan history in the cloud
- Firebase Anonymous Authentication: to securely tie cloud data to individual devices without requiring a complex login flow
- REST API/ Socket Communication: to fetch real-time product data
- Open Food Facts API: for fetching recycling detalis and instructions

  
## How to Run
1. Clone repository
2. Open with Android Studio
3. Run on emulator or device
