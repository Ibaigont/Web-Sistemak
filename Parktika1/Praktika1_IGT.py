'''
 Ibai González Tejedor
 Web Sistemak - 31
 2026ko otsailaren 22a
 1. Praktika: IoT bezeroa
 ThingSpeak-era ordenagailuaren %CPU eta %RAM datuak 15 segundoro igotzen dituen Python web bezeroa.
 Kanalaren sorkuntza automatikoa, berrerabilpena eta Ctrl+C bidezko itxiera ordenatua
 (azken 100 datuak CSV batean deskargatuz eta kanala hustuz) barne hartzen ditu.
'''

import psutil
import requests
import time
import signal
import sys
import os
import csv

ERABILTZAILE_GAKOA = "A198I8M0Q26WA2ZA"

KANAL_ID = None
IDAZTEKO_GAKOA = None


def deskargatu_eta_gorde_csv():
    print("\nAzken 100 laginak deskargatzen...")
    uria = f"https://api.thingspeak.com/channels/{KANAL_ID}/feeds.json?api_key={IDAZTEKO_GAKOA}&results=100"

    try:
        erantzuna = requests.get(uria)
        if erantzuna.status_code == 200:
            json_datuak = erantzuna.json()
            feedak = json_datuak['feeds']

            izena = "datuak.csv"
            with open(izena, mode='w', newline='') as fitxategia:
                idazlea = csv.writer(fitxategia)
                idazlea.writerow(["timestamp", "cpu", "ram"])

                for ilara in feedak:
                    idazlea.writerow([ilara['created_at'], ilara['field1'], ilara['field2']])

            print(f"Datuak '{izena}' fitxategian gorde dira.")
        else:
            print(f"Errorea datuak deskargatzean: {erantzuna.status_code}")
    except Exception as e:
        print(f"Salbuespena datuak deskargatzean: {e}")


def hustu_kanala():
    print("Kanala husten (datuak ezabatzen)...")
    uria = f"https://api.thingspeak.com/channels/{KANAL_ID}/feeds.json"
    edukia = {'api_key': ERABILTZAILE_GAKOA}

    try:
        erantzuna = requests.delete(uria, params=edukia)
        if erantzuna.status_code == 200:
            print("Kanala arrakastaz hustu da.")
        else:
            print(f"Errorea kanala hustean: {erantzuna.status_code}")
    except Exception as e:
        print(f"Salbuespena kanala hustean: {e}")


def handler(sig_num, frame):
    print('\nEtendura seinalea jaso da (Ctrl+C).')

    if KANAL_ID and IDAZTEKO_GAKOA:
        deskargatu_eta_gorde_csv()
        hustu_kanala()

    print('Programa modu ordenatuan amaitzen.')
    sys.exit(0)


def cpu_ram():
    print("Datuak bidaltzen hasten... (Gelditzeko sakatu Ctrl+C)")
    while True:
        cpu = psutil.cpu_percent(interval=15)
        ram = psutil.virtual_memory().percent
        print(f"Igotzen -> CPU: %{cpu} \tRAM: %{ram}")

        uria = "https://api.thingspeak.com/update.json"
        edukia = {
            'api_key': IDAZTEKO_GAKOA,
            'field1': cpu,
            'field2': ram
        }

        try:
            erantzuna = requests.post(uria, data=edukia)
            if erantzuna.status_code != 200:
                print(f"HTTP Errorea datuak igotzean: {erantzuna.status_code}")
        except Exception as e:
            print(f"Konexio errorea datuak igotzean: {e}")


def kargatu_kredentzialak():
    global KANAL_ID, IDAZTEKO_GAKOA
    if os.path.exists("kredentzialak.txt"):
        with open("kredentzialak.txt", "r") as fitxategia:
            lerroak = fitxategia.readlines()
            for lerroa in lerroak:
                if lerroa.startswith("Kanal_ID:"):
                    KANAL_ID = lerroa.split(":")[1].strip()
                elif lerroa.startswith("Idazteko_Gakoa:"):
                    IDAZTEKO_GAKOA = lerroa.split(":")[1].strip()
        return True
    return False


def kanala_existitzen_da():
    uria = f"https://api.thingspeak.com/channels/{KANAL_ID}.json?api_key={ERABILTZAILE_GAKOA}"
    try:
        erantzuna = requests.get(uria)
        return erantzuna.status_code == 200
    except:
        return False


def kudeatu_kanala():
    global KANAL_ID, IDAZTEKO_GAKOA

    badago_lokal = kargatu_kredentzialak()

    if badago_lokal and kanala_existitzen_da():
        print(f"Existitzen den kanala erabiliko da. ID: {KANAL_ID}")
        return

    print("Kanal berria sortzen...")
    uria = "https://api.thingspeak.com/channels.json"
    edukia = {
        'api_key': ERABILTZAILE_GAKOA,
        'name': 'Kanal_IGT',
        'field1': '%CPU',
        'field2': '%RAM'
    }

    try:
        erantzuna = requests.post(uria, data=edukia)

        if erantzuna.status_code == 200:
            json_datuak = erantzuna.json()

            KANAL_ID = json_datuak['id']
            IDAZTEKO_GAKOA = json_datuak['api_keys'][0]['api_key']

            with open("kredentzialak.txt", "w") as fitxategia:
                fitxategia.write(f"Kanal_ID: {KANAL_ID}\n")
                fitxategia.write(f"Idazteko_Gakoa: {IDAZTEKO_GAKOA}\n")
            print(f"Kanala sortu da. ID: {KANAL_ID}")

        elif erantzuna.status_code == 422:
            print("ERROREA: Kanal muga gainditu duzu. Ezabatu kanal zaharrak.")
            sys.exit(1)
        else:
            print(f"Errorea kanala sortzean: {erantzuna.status_code}")
            sys.exit(1)

    except Exception as e:
        print(f"Konexio errorea: {e}")
        sys.exit(1)


if __name__ == "__main__":
    signal.signal(signal.SIGINT, handler)
    signal.signal(signal.SIGTERM, handler)

    kudeatu_kanala()

    if IDAZTEKO_GAKOA:
        cpu_ram()