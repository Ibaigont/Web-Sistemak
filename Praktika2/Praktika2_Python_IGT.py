''''
• Ikaslearen izen-abizenak: Ibai González Tejedor
• Irakasgaia eta taldea: Web Sistemak 31
• Entrega-data: 2026ko martxoaren 22a
• Atazaren izena: 2. Praktika: Ikasgaiaren eGelan informazio bilaketa
• Entregagarriaren deskribapen laburra: eGelako zerbitzariarekin HTTP saioa ezartzen duen web bezeroa,
 "Web Sistemak" ikasgaiko PDF fitxategiak eta .py programak deskargatzen dituena,
  eta zereginen datuak CSV fitxategi batean biltzen dituena.'''
import sys
import requests
import getpass
import os
import csv
import re
from bs4 import BeautifulSoup

if len(sys.argv) != 3:
    print('Erabilera: python Praktika2_Python.py erabiltzailea "IZENA ABIZENA"')
    sys.exit(1)

erabiltzailea = sys.argv[1]
izen_abizenak = sys.argv[2]
pasahitza = getpass.getpass("Sartu zure pasahitza: ")


def inprimatu_arazketa(metodoa, uri, edukia, erantzuna):
    print(f"\n--- ESKAERA ---")
    print(f"{metodoa} {uri}")
    if edukia:
        print(f"Edukia: {edukia}")
    print("--- ERANTZUNA ---")
    print(f"{erantzuna.status_code} {erantzuna.reason}")
    if 'Set-Cookie' in erantzuna.headers:
        print(f"Set-Cookie: {erantzuna.headers['Set-Cookie']}")
    if 'Location' in erantzuna.headers:
        print(f"Location: {erantzuna.headers['Location']}")
    print("-" * 30)


def lortu_moodle_cookie(set_cookie_header):
    if not set_cookie_header:
        return ""
    zatiak = set_cookie_header.split(';')
    for zatia in zatiak:
        if 'MoodleSessionegela=' in zatia:
            return zatia.strip()
    return ""


cookie_gordea = ""

login_uri = "https://egela.ehu.eus/login/index.php"
eskaera1 = requests.get(login_uri, allow_redirects=False)

inprimatu_arazketa("GET", login_uri, None, eskaera1)

cookie_gordea = lortu_moodle_cookie(eskaera1.headers.get('Set-Cookie'))
soup1 = BeautifulSoup(eskaera1.content, 'html.parser')
logintoken_input = soup1.find('input', {'name': 'logintoken'})
logintoken = logintoken_input['value'] if logintoken_input else ""

login_post_uri = soup1.find('form', {'id': 'login'})['action']
payload = {
    'username': erabiltzailea,
    'password': pasahitza,
    'logintoken': logintoken
}
headers_2 = {'Cookie': cookie_gordea}

eskaera2 = requests.post(login_post_uri, data=payload, headers=headers_2, allow_redirects=False)

inprimatu_arazketa("POST", login_post_uri, f"username={erabiltzailea}&password=***&logintoken={logintoken[:5]}...",
                   eskaera2)

cookie_berria = lortu_moodle_cookie(eskaera2.headers.get('Set-Cookie'))
if cookie_berria:
    cookie_gordea = cookie_berria

redirect_1_uri = eskaera2.headers.get('Location')
headers_3 = {'Cookie': cookie_gordea}

eskaera3 = requests.get(redirect_1_uri, headers=headers_3, allow_redirects=False)
inprimatu_arazketa("GET", redirect_1_uri, None, eskaera3)

cookie_berria_3 = lortu_moodle_cookie(eskaera3.headers.get('Set-Cookie'))
if cookie_berria_3:
    cookie_gordea = cookie_berria_3

redirect_2_uri = eskaera3.headers.get('Location')
headers_4 = {'Cookie': cookie_gordea}

eskaera4 = requests.get(redirect_2_uri, headers=headers_4, allow_redirects=False)
inprimatu_arazketa("GET", redirect_2_uri, None, eskaera4)

profile_uri = "https://egela.ehu.eus/user/profile.php"
profile_eskaera = requests.get(profile_uri, headers={'Cookie': cookie_gordea})

if izen_abizenak.lower() in profile_eskaera.text.lower():
    print(f"\n[ONDO] Kautotzea behar bezala burutu da, '{izen_abizenak}' aurkitu da profilean!")
    input("Sakatu edozein tekla aurrera jarraitzeko...")
else:
    print(f"\n[ERROREA] Ezin izan da kautotzea baieztatu. '{izen_abizenak}' ez da aurkitu profilean.")
    sys.exit(1)

soup_dashboard = BeautifulSoup(eskaera4.content, 'html.parser')

ikasgai_esteka = None
for a in soup_dashboard.find_all('a', href=True):
    if "Web Sistemak" in a.text or "Sistemas Web" in a.text:
        ikasgai_esteka = a['href']
        break

if not ikasgai_esteka:
    print("[ERROREA] Ez da aurkitu Web Sistemak ikasgaiaren esteka.")
    sys.exit(1)

eskaera5 = requests.get(ikasgai_esteka, headers={'Cookie': cookie_gordea})
soup_ikasgaia = BeautifulSoup(eskaera5.content, 'html.parser')

csv_filename = "zereginak.csv"
csv_fitxategia = open(csv_filename, mode='w', newline='', encoding='utf-8')
csv_writer = csv.writer(csv_fitxategia, delimiter=';')
csv_writer.writerow(['Zereginaren Izenburua', 'Esteka', 'Entregatze-data'])

zereginak_gordeta = set()
deskargatuak = set()

estekak = soup_ikasgaia.find_all('a', href=True)

for esteka in estekak:
    url = esteka['href']

    if "assign/view.php" in url:
        if url in zereginak_gordeta:
            continue

        instancename = esteka.find(class_='instancename')
        if instancename:
            for hidden in instancename.find_all(class_='accesshide'):
                hidden.decompose()
            izenburua = instancename.text.strip()
        else:
            izenburua = esteka.text.strip()

        if not izenburua:
            continue

        zereginak_gordeta.add(url)

        data = "Ez da ageri orri nagusian"
        activity_item = esteka.find_parent(['li', 'div'], class_=re.compile(r'activity|modtype_assign'))
        if activity_item:
            data_elementua = activity_item.find('div', {'data-region': 'activity-dates'})
            if data_elementua:
                data = re.sub(r'\s+', ' ', data_elementua.text.strip())

        csv_writer.writerow([izenburua, url, data])

    elif "resource/view.php" in url or ".pdf" in url.lower() or ".py" in url.lower():
        if url in deskargatuak:
            continue

        gai_izena = "Gai_Ezezaguna"
        parent_section = esteka.find_parent(['li', 'section', 'div'],
                                            class_=re.compile(r'section|course-section|topics'))
        if parent_section:
            heading = parent_section.find(['h2', 'h3', 'h4'])
            if heading:
                gai_izena = heading.text.strip()

        karpeta = "".join(x for x in gai_izena if x.isalnum() or x in " -_").strip()
        if not karpeta:
            karpeta = "Gai_Ezezaguna"

        if not os.path.exists(karpeta):
            os.makedirs(karpeta, exist_ok=True)

        erantzun_fitx = requests.get(url, headers={'Cookie': cookie_gordea}, allow_redirects=True)
        deskargatuak.add(url)

        fitxategi_izena = "fitxategia"
        if "Content-Disposition" in erantzun_fitx.headers:
            match = re.search(r'filename="?([^";]+)"?', erantzun_fitx.headers["Content-Disposition"])
            if match:
                fitxategi_izena = match.group(1)
        else:
            fitxategi_izena = erantzun_fitx.url.split('/')[-1].split('?')[0]

        if fitxategi_izena.endswith('.pdf') or fitxategi_izena.endswith('.py'):
            fitx_path = os.path.join(karpeta, fitxategi_izena)
            if not os.path.exists(fitx_path):
                with open(fitx_path, 'wb') as f:
                    f.write(erantzun_fitx.content)
                print(f"[DESKARGA] {fitx_path}")

csv_fitxategia.close()
print(f"\nProzesua amaitu da. Zereginak {csv_filename} fitxategian gorde dira.")