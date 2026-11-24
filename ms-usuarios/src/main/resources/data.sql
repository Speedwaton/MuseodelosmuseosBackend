-- Cuentas de demostración. Las contraseñas están cifradas con PBKDF2 (formato sal$hash):
--   admin@museo.demo         Admin#2026
--   investigador@museo.demo  Investiga#2026
--   cliente@museo.demo       Cliente#2026
--   lucia@museo.demo         Cliente#2026
INSERT INTO usuarios (id, nombre, correo, clave_hash, rol, avatar) VALUES
(1, 'Admin Demo', 'admin@museo.demo', '2fff440aa1e93305d8e3771f95b0fec5$a86bce4abf8bce4f7b8c2438225275c469145cf13d5c4d92700c74eee2be6a65', 'ADMINISTRADOR', '🏛️'),
(2, 'Investigadora Demo', 'investigador@museo.demo', '3a017a141d778c9cb04fe1efe6aef5cd$216ff0075f9148b5928aba5b3c21a5d7ba5fe9deda01f69dfe66a24d98feb0ba', 'INVESTIGADOR', '🔍'),
(3, 'Cliente Demo', 'cliente@museo.demo', 'e61e30ab935598f0253bb3c0547dea54$04209b41084f994c9c8189361a195895eb16b3dee2de5debca3371d2ea9f383a', 'CLIENTE', '🎨'),
(4, 'Lucía Demo', 'lucia@museo.demo', '3b616f33b10d557fcec18fa19662ea06$9468339dfe1599b5ba0b31099bb1618a8bbb2af7942c6c0e3d49d07de550a925', 'CLIENTE', '🏺');
