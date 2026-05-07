-- Seed products. Prices in Tunisian Dinar (TND), based on local retailer averages
-- (Tunisianet / Mytek / Electrotek). image_url is a fallback only — admins upload
-- real images that get stored as BYTEA via the admin panel.
INSERT INTO products (id, name, category, price, description, image_url, stock) VALUES
  ('MCU-001', 'Arduino Uno R3',                  'Microcontrollers',  85.000, 'The classic 8-bit ATmega328P development board. Ideal starting point for every maker — rugged, well-documented, and endlessly reusable.', 'https://placehold.co/320x240/0D1525/06B6D4?text=Arduino+Uno+R3',          42),
  ('MCU-002', 'Arduino Nano Every',              'Microcontrollers',  45.000, 'Tiny breadboard-friendly Nano with the ATmega4809. Low cost, solid I/O, same Uno footprint in a smaller form factor.',                    'https://placehold.co/320x240/0D1525/06B6D4?text=Arduino+Nano+Every',      80),
  ('MCU-003', 'ESP32 DevKit V1',                 'Microcontrollers',  32.000, 'Dual-core Xtensa SoC with Wi-Fi and Bluetooth built in. The IoT workhorse.',                                                              'https://placehold.co/320x240/0D1525/00FF9C?text=ESP32+DevKit',           120),
  ('MCU-004', 'ESP8266 NodeMCU',                 'Microcontrollers',  18.500, 'Budget Wi-Fi microcontroller that made the IoT revolution affordable.',                                                                    'https://placehold.co/320x240/0D1525/00FF9C?text=ESP8266+NodeMCU',        200),
  ('MCU-005', 'Raspberry Pi Pico W',             'Microcontrollers',  29.900, 'RP2040 dual-core board with onboard Wi-Fi. Two PIO blocks for custom I/O magic.',                                                         'https://placehold.co/320x240/0D1525/F59E0B?text=Pi+Pico+W',              150),
  ('MCU-006', 'Raspberry Pi 5 (8GB)',            'Microcontrollers', 320.000, 'Full single-board computer powerhouse with 2.4 GHz quad-core Cortex-A76. Desktop-class performance in credit-card size.',                'https://placehold.co/320x240/0D1525/F59E0B?text=RPi+5+8GB',               18),
  ('MCU-007', 'STM32 Blue Pill',                 'Microcontrollers',  14.500, 'ARM Cortex-M3 minimal dev board. Cheap way into serious 32-bit embedded work.',                                                           'https://placehold.co/320x240/0D1525/60A5FA?text=STM32+Blue+Pill',         95),
  ('MCU-008', 'Teensy 4.1',                      'Microcontrollers', 110.000, 'The fastest Arduino-compatible board on the market. Perfect for audio DSP and high-speed sampling.',                                      'https://placehold.co/320x240/0D1525/60A5FA?text=Teensy+4.1',              25),
  ('SEN-001', 'DHT22 Temp/Humidity Sensor',      'Sensors',           22.000, 'Calibrated digital humidity and temperature sensor. Single-wire interface, rock-solid stability.',                                       'https://placehold.co/320x240/0D1525/A78BFA?text=DHT22+Sensor',           200),
  ('SEN-002', 'HC-SR04 Ultrasonic Ranger',       'Sensors',            8.500, 'Non-contact distance measurement 2 cm to 400 cm. The classic robotics eyeball.',                                                         'https://placehold.co/320x240/0D1525/A78BFA?text=HC-SR04+Ultrasonic',     400),
  ('SEN-003', 'MPU-6050 IMU 6-DOF',              'Sensors',           16.000, 'Three-axis gyroscope + three-axis accelerometer with onboard DMP. I2C, dirt cheap.',                                                     'https://placehold.co/320x240/0D1525/A78BFA?text=MPU-6050+IMU',           180),
  ('SEN-004', 'BMP280 Barometric Pressure',      'Sensors',           12.500, 'Precision pressure + temperature sensor. Great for altitude tracking and weather stations.',                                              'https://placehold.co/320x240/0D1525/A78BFA?text=BMP280+Pressure',        140),
  ('SEN-005', 'PIR Motion Sensor HC-SR501',      'Sensors',            6.500, 'Passive infrared motion detector with adjustable sensitivity and delay. Alarm projects made easy.',                                       'https://placehold.co/320x240/0D1525/A78BFA?text=PIR+HC-SR501',           320),
  ('SEN-006', 'MQ-2 Gas Sensor',                 'Sensors',           10.500, 'Detects LPG, propane, hydrogen, methane, smoke. Analog + digital output.',                                                                'https://placehold.co/320x240/0D1525/A78BFA?text=MQ-2+Gas+Sensor',        160),
  ('DSP-001', '0.96" OLED I2C 128x64',           'Displays',          18.000, 'Crisp monochrome OLED with SSD1306 driver. Tiny, power-efficient, two wires.',                                                            'https://placehold.co/320x240/0D1525/F87171?text=OLED+0.96in',            220),
  ('DSP-002', '1.3" OLED I2C 128x64',            'Displays',          26.500, 'Larger SH1106 variant for projects that need breathing room.',                                                                            'https://placehold.co/320x240/0D1525/F87171?text=OLED+1.3in',             130),
  ('DSP-003', '2.8" TFT Touch Display',          'Displays',          62.000, 'ILI9341 240x320 SPI TFT with resistive touch overlay.',                                                                                   'https://placehold.co/320x240/0D1525/F59E0B?text=TFT+2.8in+Touch',         60),
  ('DSP-004', '16x2 LCD with I2C Backpack',      'Displays',          14.000, 'Classic HD44780 character LCD wired to a PCF8574 I2C backpack — only two data pins.',                                                     'https://placehold.co/320x240/0D1525/F87171?text=16x2+LCD+I2C',           250),
  ('DSP-005', 'WS2812B LED Strip (1m)',          'Displays',          32.000, 'Individually addressable RGB LEDs. One data pin drives the whole chain.',                                                                  'https://placehold.co/320x240/0D1525/F87171?text=WS2812B+LED+Strip',       90),
  ('MOD-001', 'nRF24L01+ Wireless Module',       'Modules',            9.500, '2.4 GHz transceiver for low-power wireless telemetry. SPI controlled.',                                                                   'https://placehold.co/320x240/0D1525/06B6D4?text=nRF24L01%2B',            140),
  ('MOD-002', 'SIM800L GSM Module',              'Modules',           38.000, 'Quad-band GSM/GPRS for SMS + voice. Perfect for remote field devices.',                                                                   'https://placehold.co/320x240/0D1525/06B6D4?text=SIM800L+GSM',             45),
  ('MOD-003', 'L298N Motor Driver',              'Modules',           12.000, 'Dual H-bridge for driving two DC motors or one stepper. Up to 2A per channel.',                                                           'https://placehold.co/320x240/0D1525/06B6D4?text=L298N+Motor+Driver',     220),
  ('MOD-004', 'NEO-6M GPS Module',               'Modules',           42.000, 'u-blox NEO-6M GPS receiver with onboard antenna. NMEA over UART.',                                                                       'https://placehold.co/320x240/0D1525/06B6D4?text=NEO-6M+GPS',              55),
  ('MOD-005', 'MicroSD Card Adapter',            'Modules',            6.500, 'SPI microSD breakout. Add mass storage to any MCU project.',                                                                              'https://placehold.co/320x240/0D1525/06B6D4?text=MicroSD+Adapter',        300),
  ('MOD-006', '5V Relay Module (1-Ch)',          'Modules',            5.500, 'Optocoupled relay for switching mains loads from a microcontroller. 10A contact rating.',                                                 'https://placehold.co/320x240/0D1525/06B6D4?text=5V+Relay+Module',        380),
  ('TOL-001', 'Hakko FX-888D Soldering Station', 'Tools',            420.000, 'Professional digital soldering station. Fast tip heat-up and superb thermal recovery.',                                                   'https://placehold.co/320x240/0D1525/00FF9C?text=Hakko+FX-888D',           22),
  ('TOL-002', 'Digital Multimeter (True RMS)',   'Tools',            145.000, '6000-count auto-ranging multimeter. Cat III 600V, True RMS for clean AC readings.',                                                       'https://placehold.co/320x240/0D1525/00FF9C?text=Digital+Multimeter',      70),
  ('TOL-003', 'Breadboard 830-point',            'Tools',             14.500, 'Full-size solderless breadboard with power rails. Reusable, tried-and-true.',                                                             'https://placehold.co/320x240/0D1525/00FF9C?text=Breadboard+830pt',       220),
  ('TOL-004', 'Jumper Wire Pack (120 pcs)',      'Tools',             16.000, '40× M-M, 40× M-F, 40× F-F flexible jumpers in assorted lengths.',                                                                         'https://placehold.co/320x240/0D1525/00FF9C?text=Jumper+Wires+120pc',     400),
  ('TOL-005', 'Logic Analyzer 8-Ch 24MHz',       'Tools',             48.000, 'USB logic analyzer compatible with sigrok and PulseView. 8 channels, 24 MSa/s.',                                                          'https://placehold.co/320x240/0D1525/00FF9C?text=Logic+Analyzer+8Ch',      60),
  ('PWR-001', 'LiPo Battery 3.7V 2000mAh',       'Power',             28.000, 'Single-cell lithium-polymer pack with JST-PH connector and protection circuit.',                                                          'https://placehold.co/320x240/0D1525/F59E0B?text=LiPo+3.7V+2000mAh',       90),
  ('PWR-002', 'Buck Converter LM2596',           'Power',              4.800, 'Adjustable step-down regulator 3–40V in, 1.5–35V out, up to 3A.',                                                                         'https://placehold.co/320x240/0D1525/F59E0B?text=Buck+LM2596',            400),
  ('PWR-003', 'TP4056 LiPo Charger Module',      'Power',              3.200, 'Linear charger for single-cell Li-ion/LiPo with protection circuit.',                                                                     'https://placehold.co/320x240/0D1525/F59E0B?text=TP4056+Charger',         500),
  ('PWR-004', '5V 3A USB-C Power Supply',        'Power',             39.000, 'Clean 5V regulated supply with USB-C connector. Ideal for Pi and ESP boards.',                                                            'https://placehold.co/320x240/0D1525/F59E0B?text=5V+3A+USB-C+PSU',       110),
  ('PWR-005', 'Resistor Kit (600 pcs, 30 values)','Power',             24.500, '1/4W carbon-film resistors from 10Ω to 1MΩ. 5% tolerance, organized box.',                                                                'https://placehold.co/320x240/0D1525/F59E0B?text=Resistor+Kit+600pc',     160)
ON CONFLICT (id) DO UPDATE SET
    image_url = EXCLUDED.image_url,
    price     = EXCLUDED.price;

-- Seed product specs
INSERT INTO product_specs (product_id, spec_key, spec_value) VALUES
  ('MCU-001','MCU','ATmega328P'),('MCU-001','Clock','16 MHz'),('MCU-001','Flash','32 KB'),('MCU-001','SRAM','2 KB'),('MCU-001','I/O','14 digital, 6 analog'),('MCU-001','Voltage','5V'),
  ('MCU-002','MCU','ATmega4809'),('MCU-002','Clock','20 MHz'),('MCU-002','Flash','48 KB'),('MCU-002','SRAM','6 KB'),('MCU-002','Voltage','5V'),
  ('MCU-003','MCU','ESP32-WROOM-32'),('MCU-003','Clock','240 MHz'),('MCU-003','Flash','4 MB'),('MCU-003','Wireless','Wi-Fi + BT 4.2'),('MCU-003','GPIO','30'),
  ('MCU-004','MCU','ESP8266EX'),('MCU-004','Clock','80 MHz'),('MCU-004','Flash','4 MB'),('MCU-004','Wireless','Wi-Fi 802.11 b/g/n'),('MCU-004','GPIO','17'),
  ('MCU-005','MCU','RP2040 Dual-Core'),('MCU-005','Clock','133 MHz'),('MCU-005','Flash','2 MB'),('MCU-005','Wireless','Wi-Fi 802.11n'),('MCU-005','Voltage','3.3V'),
  ('MCU-006','CPU','Quad Cortex-A76'),('MCU-006','Clock','2.4 GHz'),('MCU-006','RAM','8 GB LPDDR4X'),('MCU-006','Ports','2 x micro-HDMI, 2 x USB-3'),('MCU-006','Wireless','Wi-Fi 6, BT 5.0'),
  ('MCU-007','MCU','STM32F103C8T6'),('MCU-007','Clock','72 MHz'),('MCU-007','Flash','64 KB'),('MCU-007','SRAM','20 KB'),('MCU-007','Voltage','3.3V'),
  ('MCU-008','MCU','ARM Cortex-M7'),('MCU-008','Clock','600 MHz'),('MCU-008','Flash','8 MB'),('MCU-008','RAM','1 MB'),('MCU-008','Ethernet','10/100'),
  ('SEN-001','Temp Range','-40 to 80C'),('SEN-001','Humidity','0-100% RH'),('SEN-001','Accuracy','+/-0.5C / +/-2% RH'),('SEN-001','Voltage','3.3-6V'),
  ('SEN-002','Range','2-400 cm'),('SEN-002','Accuracy','3 mm'),('SEN-002','Frequency','40 kHz'),('SEN-002','Voltage','5V'),
  ('SEN-003','Gyro','+/-250/500/1000/2000 deg/s'),('SEN-003','Accel','+/-2/4/8/16 g'),('SEN-003','Interface','I2C'),('SEN-003','Voltage','3.3-5V'),
  ('SEN-004','Pressure','300-1100 hPa'),('SEN-004','Accuracy','+/-1 hPa'),('SEN-004','Interface','I2C / SPI'),('SEN-004','Voltage','1.8-3.6V'),
  ('SEN-005','Range','up to 7 m'),('SEN-005','Angle','110 degrees'),('SEN-005','Voltage','4.5-20V'),('SEN-005','Output','3.3V digital'),
  ('SEN-006','Detects','LPG, CH4, H2, smoke'),('SEN-006','Voltage','5V'),('SEN-006','Output','Analog + digital'),
  ('DSP-001','Resolution','128x64'),('DSP-001','Driver','SSD1306'),('DSP-001','Interface','I2C'),('DSP-001','Voltage','3.3-5V'),
  ('DSP-002','Resolution','128x64'),('DSP-002','Driver','SH1106'),('DSP-002','Interface','I2C'),('DSP-002','Voltage','3.3-5V'),
  ('DSP-003','Resolution','240x320'),('DSP-003','Driver','ILI9341'),('DSP-003','Interface','SPI'),('DSP-003','Touch','Resistive'),
  ('DSP-004','Type','Character'),('DSP-004','Size','16x2'),('DSP-004','Interface','I2C (PCF8574)'),('DSP-004','Voltage','5V'),
  ('DSP-005','LEDs','60 per meter'),('DSP-005','Protocol','WS2812B'),('DSP-005','Voltage','5V'),('DSP-005','Color','RGB 24-bit'),
  ('MOD-001','Band','2.4 GHz'),('MOD-001','Rate','up to 2 Mbps'),('MOD-001','Interface','SPI'),('MOD-001','Voltage','3.3V'),
  ('MOD-002','Bands','Quad-band GSM'),('MOD-002','Interface','UART'),('MOD-002','Voltage','3.4-4.4V'),('MOD-002','Features','SMS, GPRS, Voice'),
  ('MOD-003','Channels','2'),('MOD-003','Current','2A'),('MOD-003','Voltage','5-35V'),('MOD-003','Logic','5V'),
  ('MOD-004','Channels','50'),('MOD-004','Accuracy','2.5 m'),('MOD-004','Interface','UART'),('MOD-004','Voltage','3.3-5V'),
  ('MOD-005','Interface','SPI'),('MOD-005','Voltage','3.3-5V'),('MOD-005','Max Card','32 GB'),('MOD-005','Format','FAT16/32'),
  ('MOD-006','Channels','1'),('MOD-006','Contact','10A @ 250VAC'),('MOD-006','Logic','5V'),('MOD-006','Isolation','Optocoupler'),
  ('TOL-001','Power','70W'),('TOL-001','Temp Range','120-480C'),('TOL-001','Display','Digital'),('TOL-001','Warranty','1 year'),
  ('TOL-002','Count','6000'),('TOL-002','Voltage','CAT III 600V'),('TOL-002','TRMS','Yes'),('TOL-002','Features','V/A/Ohm/C/Hz'),
  ('TOL-003','Tie Points','830'),('TOL-003','Size','165x55 mm'),('TOL-003','Color','White'),('TOL-003','Power Rails','2'),
  ('TOL-004','Count','120'),('TOL-004','Types','M-M, M-F, F-F'),('TOL-004','Lengths','10/20/30 cm'),
  ('TOL-005','Channels','8'),('TOL-005','Sample Rate','24 MSa/s'),('TOL-005','Interface','USB 2.0'),('TOL-005','Software','sigrok/PulseView'),
  ('PWR-001','Voltage','3.7V nominal'),('PWR-001','Capacity','2000 mAh'),('PWR-001','Connector','JST-PH'),('PWR-001','Protection','Yes'),
  ('PWR-002','Input','3-40V'),('PWR-002','Output','1.5-35V'),('PWR-002','Current','3A'),('PWR-002','Efficiency','up to 92%'),
  ('PWR-003','Charge','1A max'),('PWR-003','Input','5V micro-USB'),('PWR-003','Protection','Over-charge / over-discharge'),
  ('PWR-004','Output','5V / 3A'),('PWR-004','Connector','USB-C'),('PWR-004','Input','100-240VAC'),('PWR-004','Cable','1.5 m'),
  ('PWR-005','Count','600'),('PWR-005','Values','30 (10 Ohm-1M Ohm)'),('PWR-005','Power','0.25W'),('PWR-005','Tolerance','+/-5%')
ON CONFLICT DO NOTHING;

-- Seed default admin (password: admin123)
INSERT INTO users (id, full_name, email, password_hash, join_date, role) VALUES
  ('admin-0001', 'Site Admin', 'admin@circuithub.local',
   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
   CURRENT_DATE, 'ADMIN')
ON CONFLICT (id) DO NOTHING;
