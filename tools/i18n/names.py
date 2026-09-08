# The characters' names, for the friends album. One list per theme in tile order; the monsters
# have invented names that stay the same in every language. tools/i18n/generate.py writes them
# into values[-<locale>]/names.xml.
MONSTERS = ['Pinky', 'Fluffo', 'Grog', 'Professor Lime', 'Pumpko', 'Wizzy', 'Bruno', 'Boo', 'Hairy', 'Zombo',
            'Skully', 'Ghosty', 'Rusty', 'Bunso', 'Buggy', 'Squiddy', 'Frosty', 'Draco', 'Drooly', 'Blaze',
            'Tongo', 'Trioc', 'Huggy', 'Coral', 'Sleepy', 'Violet', 'Yelly', 'Sludge', 'Patch', 'Spiky',
            'Hornbeard', 'Cyclo', 'Grumpy', 'Growly', 'Howler', 'Greeny', 'Tusky', 'Frank', 'Bitey', 'Reddy']

NAMES = {}

NAMES['en'] = dict(
    animals=['Hen', 'Rooster', 'Chicken', 'Turkey', 'Horse', 'Cow', 'Bull', 'Goat', 'Sheep', 'Dog', 'Bunny', 'Donkey', 'Pig', 'Duck',
             'Bee', 'Goose', 'Chick', 'Parrot', 'Fly', 'Blackbird', 'Cat', 'Rat', 'Mouse', 'Hamster', 'Turtle', 'Kitten', 'Beaver', 'Chameleon'],
    emoji=['Poop', 'Okay', 'Taco', 'Thumbs up', 'Blush', 'Yummy', 'Hush', 'Cool', 'Cheeky', 'Pizza', 'Heart', 'Two hearts',
           'Grin', 'Smile', 'Oops', 'Happy cat', 'Scream', 'Angel', 'Nerd', 'Laugh', 'Hug', 'Peekaboo', 'Ears', 'Cowboy',
           'Star', 'Smiley', 'High five', 'Peace', 'Bored', 'Sick', 'Zipped', 'Thinking', 'Ill', 'Kiss', 'Hot dog', 'Cat',
           'Good luck', 'Monkey', 'Pointing', 'Hooray', 'One', 'Sleepy', 'Wow', 'Kissy', 'Sneeze', 'Fibber', 'Clown', 'Whoosh'],
    ocean=['Clownfish', 'Whale', 'Octopus', 'Crab', 'Seahorse', 'Turtle', 'Dolphin', 'Starfish', 'Jellyfish', 'Seal', 'Penguin', 'Walrus',
           'Lobster', 'Shrimp', 'Pufferfish', 'Squid', 'Stingray', 'Narwhal', 'Orca', 'Otter', 'Hermit crab', 'Clam', 'Angelfish', 'Goldfish',
           'Shark', 'Eel', 'Snail', 'Manta ray', 'Flamingo', 'Pelican', 'Seagull', 'Frog', 'Duck', 'Koi', 'Lanternfish', 'Sea urchin'],
)

NAMES['es'] = dict(
    animals=['Gallina', 'Gallo', 'Pollo', 'Pavo', 'Caballo', 'Vaca', 'Toro', 'Cabra', 'Oveja', 'Perro', 'Conejito', 'Burro', 'Cerdo', 'Pato',
             'Abeja', 'Ganso', 'Pollito', 'Loro', 'Mosca', 'Mirlo', 'Gato', 'Rata', 'Ratón', 'Hámster', 'Tortuga', 'Gatito', 'Castor', 'Camaleón'],
    emoji=['Caca', 'Vale', 'Taco', 'Pulgar arriba', 'Sonrojo', 'Rico', 'Silencio', 'Genial', 'Pícaro', 'Pizza', 'Corazón', 'Dos corazones',
           'Sonrisota', 'Sonrisa', 'Ups', 'Gato feliz', 'Grito', 'Ángel', 'Empollón', 'Risa', 'Abrazo', 'Cucú', 'Orejas', 'Vaquero',
           'Estrella', 'Carita', 'Choca esos cinco', 'Paz', 'Aburrido', 'Mareado', 'Cremallera', 'Pensando', 'Enfermo', 'Beso', 'Perrito caliente', 'Gato',
           'Buena suerte', 'Mono', 'Señalando', 'Hurra', 'Uno', 'Dormilón', 'Guau', 'Besito', 'Achís', 'Mentirosillo', 'Payaso', 'Fuuu'],
    ocean=['Pez payaso', 'Ballena', 'Pulpo', 'Cangrejo', 'Caballito de mar', 'Tortuga', 'Delfín', 'Estrella de mar', 'Medusa', 'Foca', 'Pingüino', 'Morsa',
           'Langosta', 'Gamba', 'Pez globo', 'Calamar', 'Raya', 'Narval', 'Orca', 'Nutria', 'Cangrejo ermitaño', 'Almeja', 'Pez ángel', 'Pez dorado',
           'Tiburón', 'Anguila', 'Caracol', 'Mantarraya', 'Flamenco', 'Pelícano', 'Gaviota', 'Rana', 'Pato', 'Koi', 'Pez linterna', 'Erizo de mar'],
)

NAMES['pt-rBR'] = dict(
    animals=['Galinha', 'Galo', 'Frango', 'Peru', 'Cavalo', 'Vaca', 'Touro', 'Cabra', 'Ovelha', 'Cachorro', 'Coelhinho', 'Burro', 'Porco', 'Pato',
             'Abelha', 'Ganso', 'Pintinho', 'Papagaio', 'Mosca', 'Melro', 'Gato', 'Rato', 'Camundongo', 'Hamster', 'Tartaruga', 'Gatinho', 'Castor', 'Camaleão'],
    emoji=['Cocô', 'Beleza', 'Taco', 'Joinha', 'Vergonha', 'Delícia', 'Silêncio', 'Legal', 'Travesso', 'Pizza', 'Coração', 'Dois corações',
           'Sorrisão', 'Sorriso', 'Opa', 'Gato feliz', 'Grito', 'Anjo', 'Nerd', 'Risada', 'Abraço', 'Achou', 'Orelhas', 'Caubói',
           'Estrela', 'Carinha', 'Toca aqui', 'Paz', 'Entediado', 'Enjoado', 'Zíper', 'Pensando', 'Doente', 'Beijo', 'Cachorro-quente', 'Gato',
           'Boa sorte', 'Macaco', 'Apontando', 'Viva', 'Um', 'Soninho', 'Uau', 'Beijinho', 'Atchim', 'Mentirinha', 'Palhaço', 'Vuuush'],
    ocean=['Peixe-palhaço', 'Baleia', 'Polvo', 'Caranguejo', 'Cavalo-marinho', 'Tartaruga', 'Golfinho', 'Estrela-do-mar', 'Água-viva', 'Foca', 'Pinguim', 'Morsa',
           'Lagosta', 'Camarão', 'Baiacu', 'Lula', 'Arraia', 'Narval', 'Orca', 'Lontra', 'Ermitão', 'Concha', 'Peixe-anjo', 'Peixe dourado',
           'Tubarão', 'Enguia', 'Caracol', 'Jamanta', 'Flamingo', 'Pelicano', 'Gaivota', 'Sapo', 'Pato', 'Carpa', 'Peixe-lanterna', 'Ouriço-do-mar'],
)

NAMES['fr'] = dict(
    animals=['Poule', 'Coq', 'Poulet', 'Dinde', 'Cheval', 'Vache', 'Taureau', 'Chèvre', 'Mouton', 'Chien', 'Lapin', 'Âne', 'Cochon', 'Canard',
             'Abeille', 'Oie', 'Poussin', 'Perroquet', 'Mouche', 'Merle', 'Chat', 'Rat', 'Souris', 'Hamster', 'Tortue', 'Chaton', 'Castor', 'Caméléon'],
    emoji=['Caca', 'D\'accord', 'Taco', 'Pouce levé', 'Timide', 'Miam', 'Chut', 'Cool', 'Coquin', 'Pizza', 'Cœur', 'Deux cœurs',
           'Grand sourire', 'Sourire', 'Oups', 'Chat content', 'Cri', 'Ange', 'Intello', 'Rire', 'Câlin', 'Coucou', 'Oreilles', 'Cowboy',
           'Étoile', 'Bonhomme', 'Tape m\'en cinq', 'Paix', 'Blasé', 'Malade', 'Bouche cousue', 'Réflexion', 'Fièvre', 'Bisou', 'Hot-dog', 'Chat',
           'Bonne chance', 'Singe', 'Doigt', 'Hourra', 'Un', 'Dodo', 'Ouah', 'Gros bisou', 'Atchoum', 'Menteur', 'Clown', 'Vroum'],
    ocean=['Poisson-clown', 'Baleine', 'Pieuvre', 'Crabe', 'Hippocampe', 'Tortue', 'Dauphin', 'Étoile de mer', 'Méduse', 'Phoque', 'Manchot', 'Morse',
           'Homard', 'Crevette', 'Poisson-globe', 'Calmar', 'Raie', 'Narval', 'Orque', 'Loutre', 'Bernard-l\'ermite', 'Coquillage', 'Poisson-ange', 'Poisson rouge',
           'Requin', 'Anguille', 'Escargot', 'Raie manta', 'Flamant', 'Pélican', 'Mouette', 'Grenouille', 'Canard', 'Carpe koï', 'Poisson-lanterne', 'Oursin'],
)

NAMES['de'] = dict(
    animals=['Henne', 'Hahn', 'Hühnchen', 'Truthahn', 'Pferd', 'Kuh', 'Stier', 'Ziege', 'Schaf', 'Hund', 'Hase', 'Esel', 'Schwein', 'Ente',
             'Biene', 'Gans', 'Küken', 'Papagei', 'Fliege', 'Amsel', 'Katze', 'Ratte', 'Maus', 'Hamster', 'Schildkröte', 'Kätzchen', 'Biber', 'Chamäleon'],
    emoji=['Kacki', 'Okay', 'Taco', 'Daumen hoch', 'Verlegen', 'Lecker', 'Pssst', 'Cool', 'Frech', 'Pizza', 'Herz', 'Zwei Herzen',
           'Grinsen', 'Lächeln', 'Ups', 'Frohe Katze', 'Schrei', 'Engel', 'Streber', 'Lachen', 'Umarmung', 'Kuckuck', 'Ohren', 'Cowboy',
           'Stern', 'Smiley', 'Gib fünf', 'Frieden', 'Gelangweilt', 'Übel', 'Reißverschluss', 'Nachdenklich', 'Krank', 'Kuss', 'Hotdog', 'Katze',
           'Viel Glück', 'Affe', 'Zeigefinger', 'Hurra', 'Eins', 'Schläfrig', 'Wow', 'Küsschen', 'Hatschi', 'Flunkerer', 'Clown', 'Wusch'],
    ocean=['Clownfisch', 'Wal', 'Krake', 'Krabbe', 'Seepferdchen', 'Schildkröte', 'Delfin', 'Seestern', 'Qualle', 'Robbe', 'Pinguin', 'Walross',
           'Hummer', 'Garnele', 'Kugelfisch', 'Tintenfisch', 'Rochen', 'Narwal', 'Orca', 'Otter', 'Einsiedlerkrebs', 'Muschel', 'Engelfisch', 'Goldfisch',
           'Hai', 'Aal', 'Schnecke', 'Mantarochen', 'Flamingo', 'Pelikan', 'Möwe', 'Frosch', 'Ente', 'Koi', 'Laternenfisch', 'Seeigel'],
)

NAMES['it'] = dict(
    animals=['Gallina', 'Gallo', 'Pollo', 'Tacchino', 'Cavallo', 'Mucca', 'Toro', 'Capra', 'Pecora', 'Cane', 'Coniglietto', 'Asino', 'Maiale', 'Anatra',
             'Ape', 'Oca', 'Pulcino', 'Pappagallo', 'Mosca', 'Merlo', 'Gatto', 'Ratto', 'Topo', 'Criceto', 'Tartaruga', 'Gattino', 'Castoro', 'Camaleonte'],
    emoji=['Cacca', 'Okay', 'Taco', 'Pollice su', 'Imbarazzo', 'Gnam', 'Silenzio', 'Forte', 'Birichino', 'Pizza', 'Cuore', 'Due cuori',
           'Sorrisone', 'Sorriso', 'Ops', 'Gatto felice', 'Urlo', 'Angelo', 'Secchione', 'Risata', 'Abbraccio', 'Cucù', 'Orecchie', 'Cowboy',
           'Stella', 'Faccina', 'Batti il cinque', 'Pace', 'Annoiato', 'Nausea', 'Zip', 'Pensieroso', 'Malato', 'Bacio', 'Hot dog', 'Gatto',
           'Buona fortuna', 'Scimmia', 'Ditino', 'Evviva', 'Uno', 'Sonnolento', 'Wow', 'Bacino', 'Etciù', 'Bugiardino', 'Pagliaccio', 'Vuuush'],
    ocean=['Pesce pagliaccio', 'Balena', 'Polpo', 'Granchio', 'Cavalluccio', 'Tartaruga', 'Delfino', 'Stella marina', 'Medusa', 'Foca', 'Pinguino', 'Tricheco',
           'Aragosta', 'Gambero', 'Pesce palla', 'Calamaro', 'Razza', 'Narvalo', 'Orca', 'Lontra', 'Paguro', 'Conchiglia', 'Pesce angelo', 'Pesce rosso',
           'Squalo', 'Anguilla', 'Lumaca', 'Manta', 'Fenicottero', 'Pellicano', 'Gabbiano', 'Rana', 'Anatra', 'Koi', 'Pesce lanterna', 'Riccio di mare'],
)

NAMES['ru'] = dict(
    animals=['Курица', 'Петух', 'Цыплёнок', 'Индюк', 'Лошадь', 'Корова', 'Бык', 'Коза', 'Овечка', 'Собака', 'Зайка', 'Ослик', 'Поросёнок', 'Утка',
             'Пчела', 'Гусь', 'Цыпа', 'Попугай', 'Муха', 'Дрозд', 'Кот', 'Крыса', 'Мышка', 'Хомяк', 'Черепаха', 'Котёнок', 'Бобёр', 'Хамелеон'],
    emoji=['Какашка', 'Окей', 'Тако', 'Класс', 'Смущение', 'Вкусно', 'Тсс', 'Круто', 'Шалун', 'Пицца', 'Сердце', 'Два сердца',
           'Улыбака', 'Улыбка', 'Ой', 'Довольный кот', 'Крик', 'Ангел', 'Ботаник', 'Смех', 'Обнимашки', 'Ку-ку', 'Ушки', 'Ковбой',
           'Звезда', 'Смайлик', 'Дай пять', 'Мир', 'Скучно', 'Тошнит', 'Молчок', 'Думаю', 'Болею', 'Поцелуй', 'Хот-дог', 'Кот',
           'Удачи', 'Обезьянка', 'Пальчик', 'Ура', 'Один', 'Соня', 'Вау', 'Чмок', 'Апчхи', 'Врунишка', 'Клоун', 'Вжух'],
    ocean=['Рыба-клоун', 'Кит', 'Осьминог', 'Краб', 'Морской конёк', 'Черепаха', 'Дельфин', 'Морская звезда', 'Медуза', 'Тюлень', 'Пингвин', 'Морж',
           'Омар', 'Креветка', 'Рыба-шар', 'Кальмар', 'Скат', 'Нарвал', 'Косатка', 'Выдра', 'Рак-отшельник', 'Ракушка', 'Рыба-ангел', 'Золотая рыбка',
           'Акула', 'Угорь', 'Улитка', 'Манта', 'Фламинго', 'Пеликан', 'Чайка', 'Лягушка', 'Утка', 'Карп кои', 'Рыба-фонарь', 'Морской ёж'],
)

NAMES['uk'] = dict(
    animals=['Курка', 'Півень', 'Курча', 'Індик', 'Кінь', 'Корова', 'Бик', 'Коза', 'Овечка', 'Собака', 'Зайчик', 'Ослик', 'Порося', 'Качка',
             'Бджола', 'Гусак', 'Курчатко', 'Папуга', 'Муха', 'Дрізд', 'Кіт', 'Щур', 'Мишка', 'Хом\'як', 'Черепаха', 'Кошеня', 'Бобер', 'Хамелеон'],
    emoji=['Какашка', 'Окей', 'Тако', 'Клас', 'Сором', 'Смачно', 'Тсс', 'Круто', 'Бешкетник', 'Піца', 'Серце', 'Два серця',
           'Усмішка', 'Посмішка', 'Ой', 'Веселий кіт', 'Крик', 'Янгол', 'Ботан', 'Сміх', 'Обійми', 'Ку-ку', 'Вушка', 'Ковбой',
           'Зірка', 'Смайлик', 'Дай п\'ять', 'Мир', 'Нудно', 'Нудить', 'Мовчок', 'Думаю', 'Хворію', 'Поцілунок', 'Хот-дог', 'Кіт',
           'Хай щастить', 'Мавпочка', 'Пальчик', 'Ура', 'Один', 'Соня', 'Вау', 'Цьом', 'Апчхи', 'Брехунець', 'Клоун', 'Вжух'],
    ocean=['Риба-клоун', 'Кит', 'Восьминіг', 'Краб', 'Морський коник', 'Черепаха', 'Дельфін', 'Морська зірка', 'Медуза', 'Тюлень', 'Пінгвін', 'Морж',
           'Омар', 'Креветка', 'Риба-куля', 'Кальмар', 'Скат', 'Нарвал', 'Косатка', 'Видра', 'Рак-самітник', 'Мушля', 'Риба-ангел', 'Золота рибка',
           'Акула', 'Вугор', 'Равлик', 'Манта', 'Фламінго', 'Пелікан', 'Чайка', 'Жабка', 'Качка', 'Короп кої', 'Риба-ліхтар', 'Морський їжак'],
)

NAMES['pl'] = dict(
    animals=['Kura', 'Kogut', 'Kurczak', 'Indyk', 'Koń', 'Krowa', 'Byk', 'Koza', 'Owca', 'Pies', 'Króliczek', 'Osiołek', 'Świnka', 'Kaczka',
             'Pszczoła', 'Gęś', 'Kurczaczek', 'Papuga', 'Mucha', 'Kos', 'Kot', 'Szczur', 'Myszka', 'Chomik', 'Żółw', 'Kotek', 'Bóbr', 'Kameleon'],
    emoji=['Kupka', 'Okej', 'Taco', 'Kciuk w górę', 'Rumieniec', 'Pychota', 'Ciii', 'Fajnie', 'Łobuz', 'Pizza', 'Serce', 'Dwa serca',
           'Uśmiech od ucha', 'Uśmiech', 'Ups', 'Wesoły kot', 'Krzyk', 'Aniołek', 'Kujon', 'Śmiech', 'Przytulas', 'A kuku', 'Uszy', 'Kowboj',
           'Gwiazda', 'Buźka', 'Piątka', 'Pokój', 'Nuda', 'Niedobrze', 'Zamek', 'Myślę', 'Chory', 'Buziak', 'Hot dog', 'Kot',
           'Powodzenia', 'Małpka', 'Palec', 'Hura', 'Jeden', 'Śpioch', 'Wow', 'Całusek', 'Apsik', 'Kłamczuszek', 'Klaun', 'Szuuu'],
    ocean=['Błazenek', 'Wieloryb', 'Ośmiornica', 'Krab', 'Konik morski', 'Żółw', 'Delfin', 'Rozgwiazda', 'Meduza', 'Foka', 'Pingwin', 'Mors',
           'Homar', 'Krewetka', 'Rozdymka', 'Kałamarnica', 'Płaszczka', 'Narwal', 'Orka', 'Wydra', 'Pustelnik', 'Muszla', 'Skalar', 'Złota rybka',
           'Rekin', 'Węgorz', 'Ślimak', 'Manta', 'Flaming', 'Pelikan', 'Mewa', 'Żaba', 'Kaczka', 'Karp koi', 'Rybka latarnia', 'Jeżowiec'],
)

NAMES['nl'] = dict(
    animals=['Kip', 'Haan', 'Kuiken', 'Kalkoen', 'Paard', 'Koe', 'Stier', 'Geit', 'Schaap', 'Hond', 'Konijntje', 'Ezel', 'Varken', 'Eend',
             'Bij', 'Gans', 'Kuikentje', 'Papegaai', 'Vlieg', 'Merel', 'Kat', 'Rat', 'Muis', 'Hamster', 'Schildpad', 'Katje', 'Bever', 'Kameleon'],
    emoji=['Drol', 'Oké', 'Taco', 'Duim omhoog', 'Blozen', 'Lekker', 'Ssst', 'Cool', 'Ondeugend', 'Pizza', 'Hart', 'Twee harten',
           'Grijns', 'Lach', 'Oeps', 'Blije kat', 'Schreeuw', 'Engel', 'Nerd', 'Lachen', 'Knuffel', 'Kiekeboe', 'Oren', 'Cowboy',
           'Ster', 'Smiley', 'High five', 'Vrede', 'Verveeld', 'Misselijk', 'Rits', 'Denken', 'Ziek', 'Kus', 'Hotdog', 'Kat',
           'Succes', 'Aap', 'Wijzen', 'Hoera', 'Eén', 'Slaperig', 'Wauw', 'Kusje', 'Hatsjoe', 'Jokkebrok', 'Clown', 'Woesj'],
    ocean=['Clownvis', 'Walvis', 'Octopus', 'Krab', 'Zeepaardje', 'Schildpad', 'Dolfijn', 'Zeester', 'Kwal', 'Zeehond', 'Pinguïn', 'Walrus',
           'Kreeft', 'Garnaal', 'Kogelvis', 'Inktvis', 'Rog', 'Narwal', 'Orka', 'Otter', 'Heremietkreeft', 'Schelp', 'Engelvis', 'Goudvis',
           'Haai', 'Paling', 'Slak', 'Manta', 'Flamingo', 'Pelikaan', 'Meeuw', 'Kikker', 'Eend', 'Koi', 'Lantaarnvis', 'Zee-egel'],
)

NAMES['tr'] = dict(
    animals=['Tavuk', 'Horoz', 'Piliç', 'Hindi', 'At', 'İnek', 'Boğa', 'Keçi', 'Koyun', 'Köpek', 'Tavşan', 'Eşek', 'Domuz', 'Ördek',
             'Arı', 'Kaz', 'Civciv', 'Papağan', 'Sinek', 'Karatavuk', 'Kedi', 'Sıçan', 'Fare', 'Hamster', 'Kaplumbağa', 'Kedicik', 'Kunduz', 'Bukalemun'],
    emoji=['Kaka', 'Tamam', 'Taco', 'Başparmak', 'Utangaç', 'Leziz', 'Şşş', 'Havalı', 'Yaramaz', 'Pizza', 'Kalp', 'İki kalp',
           'Sırıtış', 'Gülümseme', 'Tüh', 'Mutlu kedi', 'Çığlık', 'Melek', 'İnek', 'Kahkaha', 'Sarılma', 'Ce-e', 'Kulaklar', 'Kovboy',
           'Yıldız', 'Gülen yüz', 'Çak bir beşlik', 'Barış', 'Sıkılmış', 'Mide bulantısı', 'Fermuar', 'Düşünceli', 'Hasta', 'Öpücük', 'Sosisli', 'Kedi',
           'Bol şans', 'Maymun', 'İşaret', 'Yaşasın', 'Bir', 'Uykucu', 'Vay', 'Öpücük', 'Hapşu', 'Yalancı', 'Palyaço', 'Vuuş'],
    ocean=['Palyaço balığı', 'Balina', 'Ahtapot', 'Yengeç', 'Denizatı', 'Kaplumbağa', 'Yunus', 'Denizyıldızı', 'Denizanası', 'Fok', 'Penguen', 'Mors',
           'Deniz böceği', 'Karides', 'Balon balığı', 'Kalamar', 'Vatoz', 'Deniz gergedanı', 'Orka', 'Su samuru', 'Keşiş yengeci', 'Deniz kabuğu', 'Melek balığı', 'Japon balığı',
           'Köpekbalığı', 'Yılan balığı', 'Salyangoz', 'Manta', 'Flamingo', 'Pelikan', 'Martı', 'Kurbağa', 'Ördek', 'Koi', 'Fener balığı', 'Deniz kestanesi'],
)

NAMES['ar'] = dict(
    animals=['دجاجة', 'ديك', 'فرخة', 'ديك رومي', 'حصان', 'بقرة', 'ثور', 'ماعز', 'خروف', 'كلب', 'أرنب', 'حمار', 'خنزير', 'بطة',
             'نحلة', 'إوزة', 'كتكوت', 'ببغاء', 'ذبابة', 'شحرور', 'قطة', 'جرذ', 'فأر', 'هامستر', 'سلحفاة', 'قطة صغيرة', 'قندس', 'حرباء'],
    emoji=['كاكا', 'تمام', 'تاكو', 'إبهام', 'خجول', 'لذيذ', 'صمت', 'رائع', 'شقي', 'بيتزا', 'قلب', 'قلبان',
           'ابتسامة كبيرة', 'ابتسامة', 'أوبس', 'قطة سعيدة', 'صرخة', 'ملاك', 'نيرد', 'ضحكة', 'عناق', 'بيكابو', 'آذان', 'راعي بقر',
           'نجمة', 'وجه مبتسم', 'هات خمسة', 'سلام', 'ملل', 'غثيان', 'سحّاب', 'تفكير', 'مريض', 'قبلة', 'هوت دوغ', 'قطة',
           'حظًا سعيدًا', 'قرد', 'إشارة', 'هوراي', 'واحد', 'نعسان', 'واو', 'بوسة', 'عطسة', 'كذّاب صغير', 'مهرج', 'هووش'],
    ocean=['سمكة المهرج', 'حوت', 'أخطبوط', 'سلطعون', 'حصان البحر', 'سلحفاة', 'دلفين', 'نجم البحر', 'قنديل البحر', 'فقمة', 'بطريق', 'فظ',
           'كركند', 'جمبري', 'سمكة منتفخة', 'حبار', 'راي', 'كركدن البحر', 'أوركا', 'قضاعة', 'سلطعون ناسك', 'محارة', 'سمكة الملاك', 'سمكة ذهبية',
           'قرش', 'ثعبان البحر', 'حلزون', 'مانتا', 'فلامنغو', 'بجعة', 'نورس', 'ضفدع', 'بطة', 'كوي', 'سمكة الفانوس', 'قنفذ البحر'],
)

NAMES['hi'] = dict(
    animals=['मुर्गी', 'मुर्गा', 'चूज़ा', 'टर्की', 'घोड़ा', 'गाय', 'बैल', 'बकरी', 'भेड़', 'कुत्ता', 'खरगोश', 'गधा', 'सूअर', 'बत्तख',
             'मधुमक्खी', 'हंस', 'नन्हा चूज़ा', 'तोता', 'मक्खी', 'काली चिड़िया', 'बिल्ली', 'चूहा', 'चुहिया', 'हैम्स्टर', 'कछुआ', 'बिल्ली का बच्चा', 'ऊदबिलाव', 'गिरगिट'],
    emoji=['पॉटी', 'ठीक है', 'टाको', 'थम्स अप', 'शर्मीला', 'स्वादिष्ट', 'चुप', 'कूल', 'शरारती', 'पिज़्ज़ा', 'दिल', 'दो दिल',
           'बड़ी मुस्कान', 'मुस्कान', 'उफ़', 'खुश बिल्ली', 'चीख', 'फ़रिश्ता', 'पढ़ाकू', 'हँसी', 'गले लगना', 'पीका-बू', 'कान', 'काउबॉय',
           'तारा', 'स्माइली', 'हाई फाइव', 'शांति', 'बोर', 'जी मिचलाना', 'ज़िप', 'सोच', 'बीमार', 'चुम्मी', 'हॉट डॉग', 'बिल्ली',
           'शुभकामना', 'बंदर', 'इशारा', 'हुर्रे', 'एक', 'नींद', 'वाह', 'पप्पी', 'छींक', 'झूठा', 'जोकर', 'फुर्र'],
    ocean=['क्लाउनफ़िश', 'व्हेल', 'ऑक्टोपस', 'केकड़ा', 'समुद्री घोड़ा', 'कछुआ', 'डॉल्फ़िन', 'तारामछली', 'जेलीफ़िश', 'सील', 'पेंगुइन', 'वालरस',
           'लॉबस्टर', 'झींगा', 'पफ़रफ़िश', 'स्क्विड', 'स्टिंगरे', 'नारव्हाल', 'ऑर्का', 'ऊदबिलाव', 'हर्मिट केकड़ा', 'सीप', 'एंजेलफ़िश', 'सुनहरी मछली',
           'शार्क', 'ईल', 'घोंघा', 'मंटा रे', 'फ़्लेमिंगो', 'पेलिकन', 'सीगल', 'मेंढक', 'बत्तख', 'कोई', 'लालटेन मछली', 'समुद्री अर्चिन'],
)

NAMES['id'] = dict(
    animals=['Ayam betina', 'Ayam jago', 'Ayam', 'Kalkun', 'Kuda', 'Sapi', 'Banteng', 'Kambing', 'Domba', 'Anjing', 'Kelinci', 'Keledai', 'Babi', 'Bebek',
             'Lebah', 'Angsa', 'Anak ayam', 'Burung beo', 'Lalat', 'Burung hitam', 'Kucing', 'Tikus besar', 'Tikus', 'Hamster', 'Kura-kura', 'Anak kucing', 'Berang-berang', 'Bunglon'],
    emoji=['Pup', 'Oke', 'Taco', 'Jempol', 'Malu', 'Enak', 'Sst', 'Keren', 'Nakal', 'Pizza', 'Hati', 'Dua hati',
           'Nyengir', 'Senyum', 'Ups', 'Kucing senang', 'Teriak', 'Malaikat', 'Kutu buku', 'Tertawa', 'Peluk', 'Cilukba', 'Telinga', 'Koboi',
           'Bintang', 'Smiley', 'Tos', 'Damai', 'Bosan', 'Mual', 'Ritsleting', 'Berpikir', 'Sakit', 'Cium', 'Hot dog', 'Kucing',
           'Semoga beruntung', 'Monyet', 'Menunjuk', 'Hore', 'Satu', 'Ngantuk', 'Wow', 'Cium sayang', 'Bersin', 'Pembohong kecil', 'Badut', 'Wuss'],
    ocean=['Ikan badut', 'Paus', 'Gurita', 'Kepiting', 'Kuda laut', 'Penyu', 'Lumba-lumba', 'Bintang laut', 'Ubur-ubur', 'Anjing laut', 'Penguin', 'Walrus',
           'Lobster', 'Udang', 'Ikan buntal', 'Cumi-cumi', 'Ikan pari', 'Narwhal', 'Orca', 'Berang-berang laut', 'Kelomang', 'Kerang', 'Ikan bidadari', 'Ikan mas',
           'Hiu', 'Belut', 'Siput', 'Pari manta', 'Flamingo', 'Pelikan', 'Camar', 'Katak', 'Bebek', 'Koi', 'Ikan lentera', 'Bulu babi'],
)

NAMES['vi'] = dict(
    animals=['Gà mái', 'Gà trống', 'Gà', 'Gà tây', 'Ngựa', 'Bò', 'Bò đực', 'Dê', 'Cừu', 'Chó', 'Thỏ', 'Lừa', 'Heo', 'Vịt',
             'Ong', 'Ngỗng', 'Gà con', 'Vẹt', 'Ruồi', 'Chim sáo', 'Mèo', 'Chuột cống', 'Chuột', 'Chuột hamster', 'Rùa', 'Mèo con', 'Hải ly', 'Tắc kè'],
    emoji=['Phân', 'Ổn', 'Taco', 'Ngón cái', 'Ngại', 'Ngon', 'Suỵt', 'Ngầu', 'Nghịch', 'Pizza', 'Trái tim', 'Hai trái tim',
           'Cười toe', 'Mỉm cười', 'Ối', 'Mèo vui', 'Hét', 'Thiên thần', 'Mọt sách', 'Cười', 'Ôm', 'Ú òa', 'Tai', 'Cao bồi',
           'Ngôi sao', 'Mặt cười', 'Đập tay', 'Hòa bình', 'Chán', 'Buồn nôn', 'Kéo khóa', 'Suy nghĩ', 'Ốm', 'Hôn', 'Hot dog', 'Mèo',
           'Chúc may mắn', 'Khỉ', 'Chỉ tay', 'Hoan hô', 'Một', 'Buồn ngủ', 'Wow', 'Thơm', 'Hắt xì', 'Nói dối', 'Chú hề', 'Vèo'],
    ocean=['Cá hề', 'Cá voi', 'Bạch tuộc', 'Cua', 'Cá ngựa', 'Rùa biển', 'Cá heo', 'Sao biển', 'Sứa', 'Hải cẩu', 'Chim cánh cụt', 'Hải mã',
           'Tôm hùm', 'Tôm', 'Cá nóc', 'Mực', 'Cá đuối', 'Kỳ lân biển', 'Cá voi sát thủ', 'Rái cá', 'Ốc mượn hồn', 'Ngao', 'Cá thần tiên', 'Cá vàng',
           'Cá mập', 'Lươn', 'Ốc sên', 'Cá đuối manta', 'Hồng hạc', 'Bồ nông', 'Mòng biển', 'Ếch', 'Vịt', 'Cá koi', 'Cá đèn lồng', 'Nhím biển'],
)

NAMES['th'] = dict(
    animals=['แม่ไก่', 'ไก่โต้ง', 'ไก่', 'ไก่งวง', 'ม้า', 'วัว', 'กระทิง', 'แพะ', 'แกะ', 'หมา', 'กระต่าย', 'ลา', 'หมู', 'เป็ด',
             'ผึ้ง', 'ห่าน', 'ลูกเจี๊ยบ', 'นกแก้ว', 'แมลงวัน', 'นกดำ', 'แมว', 'หนูใหญ่', 'หนู', 'แฮมสเตอร์', 'เต่า', 'ลูกแมว', 'บีเวอร์', 'กิ้งก่า'],
    emoji=['อึ', 'โอเค', 'ทาโก้', 'ยกนิ้ว', 'เขินอาย', 'อร่อย', 'จุ๊ๆ', 'เท่', 'ซน', 'พิซซ่า', 'หัวใจ', 'สองหัวใจ',
           'ยิ้มกว้าง', 'ยิ้ม', 'อุ๊ปส์', 'แมวสุขใจ', 'กรี๊ด', 'นางฟ้า', 'เนิร์ด', 'หัวเราะ', 'กอด', 'จ๊ะเอ๋', 'หู', 'คาวบอย',
           'ดาว', 'หน้ายิ้ม', 'ไฮไฟว์', 'สันติ', 'เบื่อ', 'คลื่นไส้', 'รูดซิป', 'คิด', 'ป่วย', 'จุ๊บ', 'ฮอตดอก', 'แมว',
           'โชคดี', 'ลิง', 'ชี้', 'ไชโย', 'หนึ่ง', 'ง่วง', 'ว้าว', 'จุ๊บๆ', 'ฮัดเช่ย', 'ขี้โม้', 'ตัวตลก', 'ฟิ้ว'],
    ocean=['ปลาการ์ตูน', 'วาฬ', 'หมึกยักษ์', 'ปู', 'ม้าน้ำ', 'เต่าทะเล', 'โลมา', 'ปลาดาว', 'แมงกะพรุน', 'แมวน้ำ', 'เพนกวิน', 'วอลรัส',
           'กุ้งมังกร', 'กุ้ง', 'ปลาปักเป้า', 'ปลาหมึก', 'ปลากระเบน', 'นาร์วาล', 'วาฬเพชฌฆาต', 'นาก', 'ปูเสฉวน', 'หอย', 'ปลาเทวดา', 'ปลาทอง',
           'ฉลาม', 'ปลาไหล', 'หอยทาก', 'กระเบนแมนต้า', 'ฟลามิงโก', 'นกกระทุง', 'นกนางนวล', 'กบ', 'เป็ด', 'ปลาคาร์ป', 'ปลาโคมไฟ', 'เม่นทะเล'],
)

NAMES['ja'] = dict(
    animals=['めんどり', 'おんどり', 'にわとり', 'しちめんちょう', 'うま', 'うし', 'おうし', 'やぎ', 'ひつじ', 'いぬ', 'うさぎ', 'ろば', 'ぶた', 'あひる',
             'みつばち', 'がちょう', 'ひよこ', 'おうむ', 'はえ', 'くろどり', 'ねこ', 'ねずみ', 'こねずみ', 'ハムスター', 'かめ', 'こねこ', 'ビーバー', 'カメレオン'],
    emoji=['うんち', 'オッケー', 'タコス', 'いいね', 'てれてれ', 'おいしい', 'しーっ', 'クール', 'いたずら', 'ピザ', 'ハート', 'ダブルハート',
           'にっこり', 'えがお', 'あらら', 'うれしいねこ', 'きゃー', 'てんし', 'はかせ', 'わらい', 'ぎゅー', 'いないいないばあ', 'みみ', 'カウボーイ',
           'ほし', 'スマイル', 'ハイタッチ', 'ピース', 'つまんない', 'きもちわるい', 'チャック', 'かんがえちゅう', 'びょうき', 'キス', 'ホットドッグ', 'ねこ',
           'がんばれ', 'さる', 'ゆびさし', 'やったー', 'いち', 'ねむい', 'わあ', 'ちゅっ', 'はくしょん', 'うそつき', 'ピエロ', 'ひゅー'],
    ocean=['クマノミ', 'くじら', 'たこ', 'かに', 'タツノオトシゴ', 'うみがめ', 'いるか', 'ひとで', 'くらげ', 'あざらし', 'ペンギン', 'せいうち',
           'ロブスター', 'えび', 'ふぐ', 'いか', 'えい', 'イッカク', 'シャチ', 'ラッコ', 'やどかり', 'かい', 'エンゼルフィッシュ', 'きんぎょ',
           'さめ', 'うなぎ', 'かたつむり', 'マンタ', 'フラミンゴ', 'ペリカン', 'かもめ', 'かえる', 'あひる', 'にしきごい', 'ちょうちんあんこう', 'うに'],
)

NAMES['ko'] = dict(
    animals=['암탉', '수탉', '닭', '칠면조', '말', '소', '황소', '염소', '양', '개', '토끼', '당나귀', '돼지', '오리',
             '꿀벌', '거위', '병아리', '앵무새', '파리', '검은새', '고양이', '쥐', '생쥐', '햄스터', '거북이', '아기 고양이', '비버', '카멜레온'],
    emoji=['응가', '오케이', '타코', '엄지척', '수줍음', '맛있다', '쉿', '멋져', '장난꾸러기', '피자', '하트', '두 하트',
           '활짝 웃음', '미소', '아이쿠', '행복한 고양이', '비명', '천사', '공부벌레', '웃음', '포옹', '까꿍', '귀', '카우보이',
           '별', '스마일', '하이파이브', '평화', '지루해', '메스꺼워', '지퍼', '생각 중', '아파', '뽀뽀', '핫도그', '고양이',
           '행운을 빌어', '원숭이', '가리키기', '만세', '하나', '졸려', '와', '쪽', '에취', '거짓말쟁이', '광대', '휘익'],
    ocean=['흰동가리', '고래', '문어', '게', '해마', '바다거북', '돌고래', '불가사리', '해파리', '물개', '펭귄', '바다코끼리',
           '바닷가재', '새우', '복어', '오징어', '가오리', '일각고래', '범고래', '해달', '소라게', '조개', '엔젤피시', '금붕어',
           '상어', '뱀장어', '달팽이', '쥐가오리', '플라밍고', '펠리컨', '갈매기', '개구리', '오리', '비단잉어', '초롱아귀', '성게'],
)

NAMES['zh-rTW'] = dict(
    animals=['母雞', '公雞', '小雞', '火雞', '馬', '乳牛', '公牛', '山羊', '綿羊', '小狗', '兔子', '驢子', '小豬', '鴨子',
             '蜜蜂', '鵝', '雛雞', '鸚鵡', '蒼蠅', '黑鳥', '貓咪', '老鼠', '小老鼠', '倉鼠', '烏龜', '小貓', '海狸', '變色龍'],
    emoji=['便便', '好的', '塔可', '讚', '害羞', '好吃', '噓', '酷', '調皮', '披薩', '愛心', '雙愛心',
           '露齒笑', '微笑', '哎呀', '開心貓', '尖叫', '天使', '書呆子', '大笑', '抱抱', '躲貓貓', '耳朵', '牛仔',
           '星星', '笑臉', '擊掌', '和平', '無聊', '想吐', '拉鍊', '思考', '生病', '親親', '熱狗', '貓咪',
           '祝好運', '猴子', '指一指', '萬歲', '一', '想睡', '哇', '飛吻', '哈啾', '小騙子', '小丑', '咻'],
    ocean=['小丑魚', '鯨魚', '章魚', '螃蟹', '海馬', '海龜', '海豚', '海星', '水母', '海豹', '企鵝', '海象',
           '龍蝦', '蝦子', '河豚', '烏賊', '魟魚', '獨角鯨', '虎鯨', '海獺', '寄居蟹', '蛤蜊', '神仙魚', '金魚',
           '鯊魚', '鰻魚', '蝸牛', '鬼蝠魟', '紅鶴', '鵜鶘', '海鷗', '青蛙', '鴨子', '錦鯉', '燈籠魚', '海膽'],
)

NAMES['zh-rCN'] = dict(
    animals=['母鸡', '公鸡', '小鸡', '火鸡', '马', '奶牛', '公牛', '山羊', '绵羊', '小狗', '兔子', '驴子', '小猪', '鸭子',
             '蜜蜂', '鹅', '雏鸡', '鹦鹉', '苍蝇', '黑鸟', '猫咪', '老鼠', '小老鼠', '仓鼠', '乌龟', '小猫', '海狸', '变色龙'],
    emoji=['便便', '好的', '塔可', '点赞', '害羞', '好吃', '嘘', '酷', '调皮', '披萨', '爱心', '双爱心',
           '露齿笑', '微笑', '哎呀', '开心猫', '尖叫', '天使', '书呆子', '大笑', '抱抱', '躲猫猫', '耳朵', '牛仔',
           '星星', '笑脸', '击掌', '和平', '无聊', '想吐', '拉链', '思考', '生病', '亲亲', '热狗', '猫咪',
           '祝好运', '猴子', '指一指', '万岁', '一', '想睡', '哇', '飞吻', '阿嚏', '小骗子', '小丑', '嗖'],
    ocean=['小丑鱼', '鲸鱼', '章鱼', '螃蟹', '海马', '海龟', '海豚', '海星', '水母', '海豹', '企鹅', '海象',
           '龙虾', '虾', '河豚', '乌贼', '鳐鱼', '独角鲸', '虎鲸', '海獭', '寄居蟹', '蛤蜊', '神仙鱼', '金鱼',
           '鲨鱼', '鳗鱼', '蜗牛', '蝠鲼', '火烈鸟', '鹈鹕', '海鸥', '青蛙', '鸭子', '锦鲤', '灯笼鱼', '海胆'],
)

NAMES['ms'] = dict(
    animals=['Ayam betina', 'Ayam jantan', 'Ayam', 'Ayam belanda', 'Kuda', 'Lembu', 'Lembu jantan', 'Kambing', 'Biri-biri', 'Anjing', 'Arnab', 'Keldai', 'Babi', 'Itik',
             'Lebah', 'Angsa', 'Anak ayam', 'Burung kakak tua', 'Lalat', 'Burung hitam', 'Kucing', 'Tikus besar', 'Tikus', 'Hamster', 'Kura-kura', 'Anak kucing', 'Memerang', 'Sesumpah'],
    emoji=['Tahi', 'Okey', 'Taco', 'Ibu jari', 'Malu', 'Sedap', 'Syy', 'Hebat', 'Nakal', 'Piza', 'Hati', 'Dua hati',
           'Senyum lebar', 'Senyum', 'Alamak', 'Kucing gembira', 'Jerit', 'Malaikat', 'Ulat buku', 'Ketawa', 'Peluk', 'Cak cak', 'Telinga', 'Koboi',
           'Bintang', 'Muka senyum', 'Tepuk lima', 'Damai', 'Bosan', 'Loya', 'Zip', 'Berfikir', 'Sakit', 'Cium', 'Hot dog', 'Kucing',
           'Semoga berjaya', 'Monyet', 'Menunjuk', 'Hore', 'Satu', 'Mengantuk', 'Wah', 'Cium sayang', 'Bersin', 'Penipu kecil', 'Badut', 'Wuss'],
    ocean=['Ikan badut', 'Paus', 'Sotong kurita', 'Ketam', 'Kuda laut', 'Penyu', 'Lumba-lumba', 'Tapak sulaiman', 'Obor-obor', 'Anjing laut', 'Penguin', 'Walrus',
           'Udang kara', 'Udang', 'Ikan buntal', 'Sotong', 'Ikan pari', 'Narwhal', 'Orca', 'Memerang laut', 'Umang-umang', 'Kerang', 'Ikan bidadari', 'Ikan emas',
           'Jerung', 'Belut', 'Siput', 'Pari manta', 'Flamingo', 'Undan', 'Camar', 'Katak', 'Itik', 'Koi', 'Ikan tanglung', 'Landak laut'],
)

# The album's own words: the hint under the title, and what a shadow says when tapped.
EXTRA = {
    'es': dict(strings={'cd_more': 'Más', 'album_hint': 'Un amigo nuevo cada 10 rondas', 'friend_hidden_title': '¿Quién soy?'},
               plurals={'one': '%1$d ronda más y nos conocemos', 'other': '%1$d rondas más y nos conocemos'}),
    'pt-rBR': dict(strings={'cd_more': 'Mais', 'album_hint': 'Um amigo novo a cada 10 rodadas', 'friend_hidden_title': 'Quem sou eu?'},
                   plurals={'one': 'Mais %1$d rodada e nos conhecemos', 'other': 'Mais %1$d rodadas e nos conhecemos'}),
    'fr': dict(strings={'cd_more': 'Plus', 'album_hint': 'Un nouvel ami toutes les 10 manches', 'friend_hidden_title': 'Qui suis-je ?'},
               plurals={'one': 'Encore %1$d manche et on se rencontre', 'other': 'Encore %1$d manches et on se rencontre'}),
    'de': dict(strings={'cd_more': 'Mehr', 'album_hint': 'Alle 10 Runden ein neuer Freund', 'friend_hidden_title': 'Wer bin ich?'},
               plurals={'one': 'Noch %1$d Runde, dann treffen wir uns', 'other': 'Noch %1$d Runden, dann treffen wir uns'}),
    'it': dict(strings={'cd_more': 'Altro', 'album_hint': 'Un nuovo amico ogni 10 turni', 'friend_hidden_title': 'Chi sono?'},
               plurals={'one': 'Ancora %1$d turno e ci incontriamo', 'other': 'Ancora %1$d turni e ci incontriamo'}),
    'ru': dict(strings={'cd_more': 'Ещё', 'album_hint': 'Новый друг каждые 10 раундов', 'friend_hidden_title': 'Кто я?'},
               plurals={'one': 'Ещё %1$d раунд, и мы встретимся', 'few': 'Ещё %1$d раунда, и мы встретимся', 'many': 'Ещё %1$d раундов, и мы встретимся', 'other': 'Ещё %1$d раундов, и мы встретимся'}),
    'uk': dict(strings={'cd_more': 'Ще', 'album_hint': 'Новий друг кожні 10 раундів', 'friend_hidden_title': 'Хто я?'},
               plurals={'one': 'Ще %1$d раунд, і ми зустрінемось', 'few': 'Ще %1$d раунди, і ми зустрінемось', 'many': 'Ще %1$d раундів, і ми зустрінемось', 'other': 'Ще %1$d раундів, і ми зустрінемось'}),
    'pl': dict(strings={'cd_more': 'Więcej', 'album_hint': 'Nowy przyjaciel co 10 rund', 'friend_hidden_title': 'Kim jestem?'},
               plurals={'one': 'Jeszcze %1$d runda i się poznamy', 'few': 'Jeszcze %1$d rundy i się poznamy', 'many': 'Jeszcze %1$d rund i się poznamy', 'other': 'Jeszcze %1$d rund i się poznamy'}),
    'nl': dict(strings={'cd_more': 'Meer', 'album_hint': 'Elke 10 rondes een nieuwe vriend', 'friend_hidden_title': 'Wie ben ik?'},
               plurals={'one': 'Nog %1$d ronde en we ontmoeten elkaar', 'other': 'Nog %1$d rondes en we ontmoeten elkaar'}),
    'tr': dict(strings={'cd_more': 'Daha fazla', 'album_hint': 'Her 10 turda yeni bir arkadaş', 'friend_hidden_title': 'Ben kimim?'},
               plurals={'one': '%1$d tur daha, sonra tanışırız', 'other': '%1$d tur daha, sonra tanışırız'}),
    'ar': dict(strings={'cd_more': 'المزيد', 'album_hint': 'صديق جديد كل 10 جولات', 'friend_hidden_title': 'من أنا؟'},
               plurals={'zero': 'نلتقي بعد %1$d جولة', 'one': 'جولة واحدة ونلتقي', 'two': 'جولتان ونلتقي', 'few': '%1$d جولات ونلتقي', 'many': '%1$d جولة ونلتقي', 'other': '%1$d جولة ونلتقي'}),
    'hi': dict(strings={'cd_more': 'और', 'album_hint': 'हर 10 राउंड पर एक नया दोस्त', 'friend_hidden_title': 'मैं कौन हूँ?'},
               plurals={'one': 'बस %1$d राउंड और, फिर मिलेंगे', 'other': 'बस %1$d राउंड और, फिर मिलेंगे'}),
    'id': dict(strings={'cd_more': 'Lainnya', 'album_hint': 'Teman baru setiap 10 ronde', 'friend_hidden_title': 'Siapa aku?'},
               plurals={'other': '%1$d ronde lagi dan kita bertemu'}),
    'vi': dict(strings={'cd_more': 'Thêm', 'album_hint': 'Cứ 10 vòng có một bạn mới', 'friend_hidden_title': 'Tớ là ai?'},
               plurals={'other': 'Thêm %1$d vòng nữa là gặp tớ'}),
    'th': dict(strings={'cd_more': 'เพิ่มเติม', 'album_hint': 'เพื่อนใหม่ทุก 10 รอบ', 'friend_hidden_title': 'ฉันคือใคร'},
               plurals={'other': 'อีก %1$d รอบก็ได้เจอกัน'}),
    'ja': dict(strings={'cd_more': 'もっと', 'album_hint': '10ラウンドごとに あたらしい なかま', 'friend_hidden_title': 'だれかな？'},
               plurals={'other': 'あと %1$d ラウンドで あえるよ'}),
    'ko': dict(strings={'cd_more': '더 보기', 'album_hint': '10라운드마다 새 친구', 'friend_hidden_title': '나는 누구일까?'},
               plurals={'other': '%1$d라운드만 더 하면 만나요'}),
    'zh-rTW': dict(strings={'cd_more': '更多', 'album_hint': '每 10 關多一位新朋友', 'friend_hidden_title': '我是誰？'},
                   plurals={'other': '再玩 %1$d 關就能見到我'}),
    'zh-rCN': dict(strings={'cd_more': '更多', 'album_hint': '每 10 关多一位新朋友', 'friend_hidden_title': '我是谁？'},
                   plurals={'other': '再玩 %1$d 关就能见到我'}),
    'ms': dict(strings={'cd_more': 'Lagi', 'album_hint': 'Kawan baru setiap 10 pusingan', 'friend_hidden_title': 'Siapa saya?'},
               plurals={'other': '%1$d pusingan lagi dan kita berjumpa'}),
}
