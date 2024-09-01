import Link from "next/link"
import SearchBar from "../components/SearchBar"


const Home: React.FC = () => {
  const handleSearch = (searchTerm: string) => {
    console.log('Searching for:', searchTerm);
    // Add your search logic here
  };

  return <div className="width-[70%]">
    <SearchBar onSearch={handleSearch}></SearchBar>
  </div>
}

export default Home;