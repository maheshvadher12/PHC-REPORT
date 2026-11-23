APK બનાવવાની રીત (GitHub Actions થી - કમ્પ્યુટર પર કંઈ ઇન્સ્ટોલ કરવાની જરૂર નથી)

1) github.com માં નવું Repository બનાવો (જેમ કે lab-report-apk).
2) આ ZIP ને Extract કરો. LabReport-APK ફોલ્ડરની અંદરની બધી ફાઈલો/ફોલ્ડર GitHub માં
   Add file > Upload files થી ખેંચીને મૂકો. (.github ફોલ્ડર પણ સાથે જવું જોઈએ.)
   જો .github ફોલ્ડર ન દેખાય/ન જાય તો: Add file > Create new file > નામમાં
   .github/workflows/build-apk.yml લખો અને ફાઈલની અંદરનું લખાણ પેસ્ટ કરો.
3) Commit changes દબાવો.
4) ઉપર "Actions" ટેબ ખોલો. "Build APK" ચાલુ થશે (5-8 મિનિટ). ગ્રીન ટીક આવે એટલે પૂરું.
   ન ચાલે તો Actions > Build APK > Run workflow.
5) પૂરું થયેલા રનમાં નીચે "Artifacts" માં LabReport-APK ડાઉનલોડ કરો (ZIP છે). Extract કરો તો app-debug.apk મળશે.
6) APK ને ફોનમાં ઇન્સ્ટોલ કરો (Unknown sources ની પરવાનગી આપવી પડશે).

અપડેટ કરવા: app/src/main/assets/index.html ફાઈલ GitHub માં નવી ફાઈલથી બદલો > Commit.
નવી APK ફરી બનશે અને ફોનમાં જૂની એપ પર જ ઇન્સ્ટોલ કરી શકાશે.
(વર્ઝન વધારવા app/build.gradle માં versionCode 1 નું 2, 3... કરો.)

આ એપમાં: Print બટન સીધું Android નું પ્રિન્ટ વિન્ડો ખોલે છે, PDF Downloads માં સેવ થાય છે,
Back દબાવતાં "Press back again to exit" આવે છે, ફોન આડો/ઊભો થાય તો ડેટા જળવાય છે.
